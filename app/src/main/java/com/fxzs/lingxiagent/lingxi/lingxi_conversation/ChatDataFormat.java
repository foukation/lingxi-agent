package com.fxzs.lingxiagent.lingxi.lingxi_conversation;

import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;
import com.cmdc.ai.assist.constraint.DialogueResult;
import com.example.device_control.AgentResult;
import com.example.device_control.SchedulerManagerFactory;
import com.example.service_api.HttpUrlConnectionHonor;
import com.fxzs.lingxiagent.R;
import com.fxzs.lingxiagent.helper.AppListHelper;
import com.fxzs.lingxiagent.lingxi.config.ChatFlowCallback;
import com.fxzs.lingxiagent.lingxi.main.utils.GsonUtils;
import com.fxzs.lingxiagent.lingxi.multimodal.utils.TtsMediaPlayer;
import com.fxzs.lingxiagent.util.MediaPlayerUtils;
import com.fxzs.lingxiagent.util.TtsXiaDuMediaPlayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

import timber.log.Timber;

public class ChatDataFormat {
	private static final String TAG = "ChatDataFormat";
	public LocalModule curModule = null;
	public LocalModule mainCurModule = null;
	boolean isBreakFlow = false;
	private DialogueResult result = null;
	private SchedulerManagerFactory schedulerManagerFactory = null;
	private final WeakReference<Activity> activityRef;

    public ChatDataFormat(Activity activityRef) {
		this.activityRef = new WeakReference<>(activityRef);

	}

    public void init() {
		MediaPlayerUtils.getInstance().release();
		TtsXiaDuMediaPlayer.getInstance().stop();
		TtsMediaPlayer.getInstance().stop();

		this.curModule = null;
		this.isBreakFlow = false;
		getMainCurModule();
	}

	private void getMainCurModule() {
		mainCurModule = TabEntity.matchLocalModule(TabEntity.agentType);
	}

	/**
	 * 启动对话流
	 * 根据对话结果和当前模块状态，执行相应的操作或关闭对话流
	 *
	 * @param result 对话结果对象，包含对话的相关信息和状态
	 */
	public void startFlow(DialogueResult result, ChatFlowCallback callback) {
		Timber.tag(TAG).d("DialogueResult:%s", result);
		int isEnd = result.is_end();

		// 检查是否需要中断对话流
		if (!isBreakFlow) {
			// 分发意图，确定当前模块
			LocalModule module = intentDistribute();
			// 更新当前对话结果
			this.result = result;

			// 如果分发到的模块不为空，则更新当前模块
			if (module != null) {
				curModule = module;
			}

			// 如果主模块不为空且不是聊天模块，则优先使用主模块
			if (mainCurModule != null && mainCurModule != LocalModule.CHAT) {
				curModule = mainCurModule;
			}

			// 根据当前模块执行相应的操作
			if (curModule == LocalModule.TRAVEL) {
				return;
			}

			if (curModule == LocalModule.TRIP) {
				return;
			}

			if (curModule == LocalModule.GATHERING) {
				return;
			}

			if (curModule == LocalModule.SYS_CONTROL) {
				execScheduler(callback);
				return;
			}

			if (curModule == LocalModule.ACTION) {
				return;
			}

			if (curModule == LocalModule.IMG) {
				String percent = getPercentData();
				ArrayList<String> imageList = getImgData();
				if (!percent.isEmpty()) {
					callback.receive(LocalModule.CHAT, false, percent);
				}
				if (imageList != null) {
					callback.receive(LocalModule.IMG, true, imageList);
				}
			}

			if (curModule == LocalModule.MEDIA) {
				execScheduler(callback);
				return;
			}

			if (curModule == LocalModule.MUSIC){
				isBreakFlow = true;
				callback.receive(LocalModule.SYS_CONTROL, isBreakFlow, activityRef.get().getString(R.string.exec_sys_control_default));
				return;
			}

			if (curModule == LocalModule.WEATHER) {
				String lastAnswer = result.getAssistant_answer_content();
				if (lastAnswer != null) {
					callback.receive(LocalModule.CHAT, false, lastAnswer);
				}
			}

			if (curModule == LocalModule.CHAT) {
				String answer = getAnswerData();
				if (!answer.isEmpty()) {
					callback.receive(LocalModule.CHAT, false, answer);
				}
			}
		}

		// 如果对话结束，更新状态并执行兜底操作
		if (isEnd == 1) {
			callback.end();
		}
	}

	/**
	 * 分发意图处理
	 * 该方法根据意图(domain和intent)将处理流程分发到不同的本地模块
	 * 主要解析结果中的header和payload，提取name和nlu信息，根据domain和intent决定下一步的处理模块
	 *
	 * @return LocalModule 根据不同的意图返回对应的本地模块处理类，如果没有匹配的模块则返回null
	 */
	private LocalModule intentDistribute() {
		try {
			// 获取结果的头部信息
			JSONObject header = result.getHeader();
			if (header == null) {
				return null;
			}
			// 获取头部中的name字段
			String name = header.optString("name");
			// 检查name是否匹配自然语言理解(NLU)的别名
			if (name.equals(NameType.NLU.getAlias())) {
				// 获取结果的负载信息
				JSONObject payload = result.getPayload();
				assert payload != null;
				// 获取负载中的nlu数组
				JSONArray nlu = payload.optJSONArray("nlu");
				if (nlu != null) {
					// 获取nlu数组中的第一个元素
					JSONObject nluFir = (JSONObject) nlu.get(0);
					// 提取domain和intent字段
					String domain = nluFir.optString("domain");
					String intent = nluFir.optString("intent");
					// 根据domain和intent的值决定返回的本地模块
					if (domain.equals(IntentDomain.CHAT.getAlias())) {
						// 处理聊天相关的意图
						if (intent.equals(ChatIntent.TRANSLATION.getAlias())) {
							return LocalModule.TRANSLATE;
						} else if (intent.equals(ChatIntent.LLMQA.getAlias())) {
							return LocalModule.CHAT;
						} else if (intent.equals(ChatIntent.WEATHER.getAlias())) {
							return LocalModule.WEATHER;
						} else if (intent.equals(ChatIntent.BAIDU_BAIKE.getAlias())) {
							return LocalModule.CHAT;
						} else {
							return LocalModule.CHAT;
						}
					} else if (domain.equals(IntentDomain.AIGC.getAlias())) {
						// 处理AI生成内容相关的意图
						if (intent.equals(ImgIntent.AIGC_DRAW.getAlias())) {
							return LocalModule.IMG;
						} else {
							return LocalModule.CHAT;
						}
					} else if (domain.equals(IntentDomain.MEDIA.getAlias())) {
						// 处理媒体相关的意图
						if (intent.equals(MediaIntent.MEDIA_VIDEOPLY.getAlias()) | intent.equals(MediaIntent.MEDIA_UNICAST.getAlias())) {
							if (schedulerManagerFactory == null) {
								schedulerManagerFactory = new SchedulerManagerFactory(activityRef.get());
								schedulerManagerFactory.setAppList(Objects.requireNonNull(GsonUtils.toJson(AppListHelper.INSTANCE.getAppInfoList())));
							}
							schedulerManagerFactory.updateIntent(nlu.toString(), domain);
							return LocalModule.MEDIA;
						} else if (intent.equals(MediaIntent.MEDIA_MUSIC.getAlias())) {
							return LocalModule.MUSIC;
						} else {
							return LocalModule.CHAT;
						}
					} else if (
							domain.equals(IntentDomain.SYSTEM_CONTROL.getAlias()) |
									domain.equals(IntentDomain.PHONE.getAlias()) |
									domain.equals(IntentDomain.CAR_CONTROL.getAlias()) |
									domain.equals(IntentDomain.ALARM.getAlias()) |
									domain.equals(IntentDomain.TELECOMSERVICE.getAlias()) |
									domain.equals(IntentDomain.HEALTHCARE.getAlias()) |
									domain.equals(IntentDomain.CUSTOMERSERVICE.getAlias()) |
									domain.equals(IntentDomain.MEMBERSHIP.getAlias())) {
						// 处理系统控制、电话和汽车控制相关的意图
						if (schedulerManagerFactory == null) {
							schedulerManagerFactory = new SchedulerManagerFactory(activityRef.get());
							schedulerManagerFactory.setAppList(Objects.requireNonNull(GsonUtils.toJson(AppListHelper.INSTANCE.getAppInfoList())));
						}
						schedulerManagerFactory.updateIntent(nlu.toString(), domain);
						return LocalModule.SYS_CONTROL;
					} else if (domain.equals(IntentDomain.NAVIGATION.getAlias())) {
						// 处理导航相关的意图
						if (intent.equals(NavIntent.NAV_AIGuide.getAlias())) {
							return LocalModule.TRAVEL;
						} else if (intent.equals(NavIntent.NAV_POI.getAlias())) {
							return LocalModule.TRIP;
						} else if (intent.equals(NavIntent.NAV_NAV.getAlias())) {
							return LocalModule.ACTION;
						} else {
							return LocalModule.CHAT;
						}
					} else if (domain.equals(IntentDomain.ALARM.getAlias())) {
						// 处理闹钟相关的意图
						return LocalModule.ACTION;
					} else if (domain.equals(IntentDomain.DRINK.getAlias())) {
						// 处理饮料相关的意图
						return LocalModule.CHAT;
					} else if (domain.equals(IntentDomain.UNCLEAR.getAlias())) {
						// 处理不明确的意图
						return LocalModule.WEATHER;
					} else if (domain.equals(IntentDomain.TRAVEL.getAlias())) {
						// 处理不明确的意图
						return LocalModule.TRAVEL;
					} else {
						// 默认处理聊天意图
						return LocalModule.CHAT;
					}
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public void addChatView() {
	}

	public void addChatWeatherView() {
	}

	public void addImgView() {
	}

	public void addMusicView() {
	}

	public void execScheduler(ChatFlowCallback callback) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			String resultStr;
			AgentResult agentResult = schedulerManagerFactory.start();
			if (agentResult.getResult()) {
				resultStr = !Objects.equals(agentResult.getSucMsg(), "") ? agentResult.getSucMsg() : "指令执行成功";
			} else {
				resultStr = !Objects.equals(agentResult.getErrMsg(), "") ? agentResult.getErrMsg() : "指令执行失败";
			}

			if (!TextUtils.isEmpty(resultStr)) {
				isBreakFlow = true;
				callback.receive(LocalModule.SYS_CONTROL, isBreakFlow, resultStr);
			}
		}
	}

	public void execTranslate() {
	}

	public void execTripHonor(HttpUrlConnectionHonor HonorHttp) {
	}

	private void showHonorCard(BodyData body) {
	}

	private void showHonorServiceCard(CardData cardData) {
	}

	public void execAction() {
	}

	private ArrayList<String> getImgData() {
		try {
			JSONObject header = result.getHeader();
			JSONObject payload = result.getPayload();
			if (header != null) {
				String name = header.optString("name");
				if (payload != null && NameType.IMG_CARD.getAlias().equals(name)) {
					JSONArray urlsArray = payload.optJSONArray("urls");
					ArrayList<String> addressList = new ArrayList<>();
					if (urlsArray != null) {
						for (int i = 0; i < urlsArray.length(); i++) {
							addressList.add(urlsArray.optString(i));
						}
					}
					return addressList;
				}
			}
			return null;
		} catch (Exception ignored) {
			return null;
		}
	}

	private String getPercentData() {
		try {
			JSONObject header = result.getHeader();
			JSONObject payload = result.getPayload();
			if (header != null) {
				String name = header.optString("name");
				if (NameType.PROCESS.getAlias().equals(name)) {
					assert payload != null;
					return "图片生成中：" + payload.optInt("percent") + "%";
				}
			}
			return "";
		} catch (Exception e) {
			return "";
		}
	}

	private String getAnswerData() {
		try {
			JSONObject header = result.getHeader();
			JSONObject payload = result.getPayload();
			if (header != null) {
				String name = header.optString("name");
				if (NameType.RENDER_FLOW.getAlias().equals(name)) {
					assert payload != null;
					return payload.optString("answer");
				}
			}

		} catch (Exception e) {
			return "";
		}
		return "";
	}

	private String getSpeakData() {
		try {
			JSONObject header = result.getHeader();
			JSONObject payload = result.getPayload();
			if (header != null) {
				String name = header.optString("name");
				if (NameType.SPEAK.getAlias().equals(name)) {
					assert payload != null;
					return payload.optString("url");
				}
			}

		} catch (Exception e) {
			return "";
		}
		return "";
	}
}