package com.fxzs.lingxiagent.lingxi.lingxi_conversation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;


import androidx.annotation.NonNull;

import com.cmdc.ai.assist.constraint.DialogueResult;
import com.example.device_control.AgentResult;
import com.example.device_control.SchedulerManagerFactory;
import com.example.service_api.HttpUrlConnectionHonor;
import com.example.service_api.config.HonorConf;
import com.fxzs.lingxiagent.actions.HandlerLlm;
import com.fxzs.lingxiagent.helper.AppListHelper;
import com.fxzs.lingxiagent.lingxi.main.utils.BroadcastUtils;
import com.fxzs.lingxiagent.lingxi.main.utils.GsonUtils;
import com.fxzs.lingxiagent.lingxi.multimodal.utils.TtsMediaPlayer;
import com.fxzs.lingxiagent.lingxi.translate.TranslationManager;
import com.fxzs.lingxiagent.util.GlobalDataHolder;
import com.fxzs.lingxiagent.util.MediaPlayerUtils;
import com.fxzs.lingxiagent.util.TtsXiaDuMediaPlayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

public class ChatDataFormat {
	private static final String TAG = "ChatDataFormat";
	private String AsrRespStr = "";
	private SchedulerManagerFactory schedulerManagerFactory = null;
	private DialogueResult result = null;
	private Activity context = null;
	public LocalModule curModule = null;
	public LocalModule mainCurModule = null;
	boolean lastExecSuc = false;
	boolean isBreakFlow = false;
	BroadcastUtils Utils;
	private String fromLang, toLang;
	private boolean chatReplyIsFirst = true;

	public void init(Activity context, String AsrRespStr) {
		Timber.tag(TAG).d("initServer");
		MediaPlayerUtils.getInstance().release();
		TtsXiaDuMediaPlayer.getInstance().stop();
		TtsMediaPlayer.getInstance().stop();

		this.context = context;
		this.AsrRespStr = AsrRespStr;
		this.Utils = BroadcastUtils.INSTANCE;
		this.curModule = null;
		this.mainCurModule = null;
		this.isBreakFlow = false;
		this.chatReplyIsFirst = true;
		getMainCurModule();
	}

	public void setTranslationLanguage(String fromLang, String toLang) {
		this.fromLang = fromLang;
		this.toLang = toLang;
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
	public void startFlow(DialogueResult result, HttpUrlConnectionHonor honorHttp) {
		int isEnd = result.is_end();
		// 检查是否需要中断对话流
		if (!isBreakFlow) {
//			TimberUtils.logLong(TAG, String.valueOf(result));
			// 分发意图，确定当前模块
			LocalModule module = intentDistribute();
			// 更新当前对话结果
			this.result = result;
			// 判断对话是否结束


			// 如果分发到的模块不为空，则更新当前模块
			if (module != null) {
				curModule = module;
			}
			// 如果主模块不为空且不是聊天模块，则优先使用主模块
			if (mainCurModule != null && mainCurModule != LocalModule.CHAT) {
				curModule = mainCurModule;
			}
			Timber.tag(TAG).d("curModule:%s", curModule);
			// 根据当前模块执行相应的操作
			if (curModule == LocalModule.TRAVEL) {
				startFlow(LocalModule.TRAVEL, honorHttp);
				closeFlow();
				return;
			}
			if (curModule == LocalModule.TRIP) {
				startFlow(LocalModule.TRIP, honorHttp);
				closeFlow();
				return;
			}
			if (curModule == LocalModule.GATHERING) {
				startFlow(LocalModule.TRIP, honorHttp);
				closeFlow();
				return;
			}
			if (curModule == LocalModule.SYS_CONTROL) {
				execScheduler();
				closeFlow();
				if (endListener != null) {
					endListener.onCancel(true);
				}
				return;
			}
			if (curModule == LocalModule.ACTION) {
				execAction();
				closeFlow();
				if (endListener != null) {
					endListener.onCancel(true);
				}
				return;
			}
			if (curModule == LocalModule.IMG) {
				addImgView();
			}
			if (curModule == LocalModule.MEDIA) {
				execScheduler();
				closeFlow();
				if (endListener != null) {
					endListener.onCancel(true);
				}
				return;
			}
			if (curModule == LocalModule.MUSIC){//音乐
				addMusicView();
			}
			if (curModule == LocalModule.WEATHER) {
				addChatWeatherView();
			}
			if (curModule == LocalModule.CHAT) {
				addChatView();
			}
			// 如果对话结束，更新状态并执行兜底操作
			if (isEnd == 1) {
				closeFlow();
				Utils.completeBroadcast();
				if (endListener != null) {
					endListener.onCancel(true);
				}
			}
		}
	}

	public void startFlow(LocalModule module, HttpUrlConnectionHonor HonorHttp) {
		// 检查是否需要中断对话流
		if (!isBreakFlow) {
			// 如果分发到的模块不为空，则更新当前模块
			if (module != null) {
				curModule = module;
			}
			Timber.tag(TAG).d("curModule:%s", curModule);
			// 根据当前模块执行相应的操作
			if (curModule == LocalModule.TRIP) {
				HonorHttp.updateRequestInfo(HonorConf.HONOR_MEET);
				execTripHonor(HonorHttp);
				closeFlow();
				return;
			}
			if (curModule == LocalModule.TRAVEL) {
				HonorHttp.updateRequestInfo(HonorConf.HONOR_TRIP);
				execTripHonor(HonorHttp);
				closeFlow();
				return;
			}
			if (curModule == LocalModule.TRIP_HONOR) {
				HonorHttp.updateRequestInfo(HonorConf.HONOR_MEET);
				execTripHonor(HonorHttp);
				closeFlow();
				return;
			}
			if (curModule == LocalModule.MEDICINE) {
				execAction();
				closeFlow();
			}
		}
	}

	public void startFlow(LocalModule module) {
		// 检查是否需要中断对话流
		if (!isBreakFlow) {
			// 如果分发到的模块不为空，则更新当前模块
			if (module != null) {
				curModule = module;
			}
			// 根据当前模块执行相应的操作
			if (curModule == LocalModule.TRANSLATE) {
				execTranslate();
			}
		}
	}

	private void closeFlow() {
		isBreakFlow = true;
		Utils.closeAsrBroadcast();
	}

	public boolean isBreakFlow() {
		return isBreakFlow;
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
								schedulerManagerFactory = new SchedulerManagerFactory(context);
								schedulerManagerFactory.setAppList(Objects.requireNonNull(GsonUtils.toJson(AppListHelper.INSTANCE.getAppInfoList())));
							}
							schedulerManagerFactory.updateIntent(nlu.toString(), domain);
							return LocalModule.MEDIA;
						}
						else if (intent.equals(MediaIntent.MEDIA_MUSIC.getAlias()) ){
							return LocalModule.MUSIC;
						}
						else {
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
						closeFlow();
						if (schedulerManagerFactory == null) {
							schedulerManagerFactory = new SchedulerManagerFactory(context);
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
							closeFlow();
							return LocalModule.ACTION;
						} else {
							return LocalModule.CHAT;
						}
					} else if (domain.equals(IntentDomain.ALARM.getAlias())) {
						// 处理闹钟相关的意图
						closeFlow();
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
		String answer = getAnswerData();
		String speak = getSpeakData();


		if (!answer.isEmpty()) {
			if (chatReplyIsFirst) {
				Utils.replyTypeWriteContentBroadcast(answer);
				chatReplyIsFirst = false;
			} else {
//				MessageAdapter adapter = ChatManager.messageAdapter;
//				int idx = adapter.getItemCount() - 1;
//				adapter.startTypeTextAt(idx, answer);
			}
			lastExecSuc = true;
		}

		boolean defaultEnableTts = GlobalDataHolder.getDefaultEnableTts();
		if (!speak.isEmpty() && defaultEnableTts) {
//			MessageAdapter adapter = ChatManager.messageAdapter;
//			int idx = adapter.getItemCount() - 1;
//			adapter.setAudioPlayStatus(idx, true, true);
//
//			String qid = result.getQid();
//			TtsXiaDuMediaPlayer.getInstance().speak(qid, speak);
//			TtsXiaDuMediaPlayer.getInstance().setOnPlayerStopListener(() -> adapter.setAudioPlayStatus(idx, false, true));
		}
	}

	public void addChatWeatherView() {
		String speak = getSpeakData();
		String lastAnswer = result.getAssistant_answer_content();
		if (lastAnswer != null && !lastAnswer.isEmpty()) {
			Utils.replyTypeWriteContentBroadcast(lastAnswer);
			lastExecSuc = true;
		}

		boolean defaultEnableTts = GlobalDataHolder.getDefaultEnableTts();
		if (!speak.isEmpty() && defaultEnableTts) {
//			MessageAdapter adapter = ChatManager.messageAdapter;
//			int idx = adapter.getItemCount() - 1;
//			adapter.setAudioPlayStatus(idx, true, false);

			String qid = result.getQid();
			TtsXiaDuMediaPlayer.getInstance().speak(qid, speak);
//			TtsXiaDuMediaPlayer.getInstance().setOnPlayerStopListener(() -> adapter.setAudioPlayStatus(idx, false, false));
		}
		if (endListener != null) {
			endListener.onCancel(true);
		}
	}

	public void addImgView() {
		String percent = getPercentData();
		ArrayList<String> imagesPath = getImgData();

		if (!percent.isEmpty()) {
			Utils.replyMessageBroadcast(percent, true);
		}

		if (imagesPath != null && !imagesPath.isEmpty()) {
			Utils.replyMessageBroadcast("图片已为您生成，点击可预览", true);
			Utils.replyImagesBroadcast(imagesPath);
			lastExecSuc = true;
			closeFlow();
			if (endListener != null) {
				endListener.onCancel(true);
			}
		}
	}

	public void addMusicView() {
		Map<String, String> play = getPlayData();
		if (play != null) {
			MediaPlayerUtils.getInstance().create();
			String url = play.get("url");
			String albumName = play.get("albumName");
			if (albumName != null && url != null) {
				Utils.replyMediaBroadcast(albumName, url);
				lastExecSuc = true;
				closeFlow();
			}
			if (endListener != null) {
				endListener.onCancel(true);
			}
		}
	}

	public void execScheduler() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			String resultStr;
			AgentResult agentResult = schedulerManagerFactory.start();
			if (agentResult.getResult()) {
				resultStr = !Objects.equals(agentResult.getSucMsg(), "") ? agentResult.getSucMsg() : "指令执行成功";
			} else {
				resultStr = !Objects.equals(agentResult.getErrMsg(), "") ? agentResult.getErrMsg() : "指令执行失败";
			}
			assert resultStr != null;
			Utils.replyMessageBroadcast(resultStr, true);
			Utils.completeBroadcast();
			lastExecSuc = true;

		}
	}

	public void execTranslate() {
		lastExecSuc = true;
		TranslationManager translationManager = new TranslationManager(context, aBoolean -> {
			if (endListener != null) {
				endListener.onCancel(true);
			}
			return null;
		});
		translationManager.translation(fromLang, toLang);
	}

	public void execTripHonor(HttpUrlConnectionHonor HonorHttp) {
		lastExecSuc = true;
		StringBuilder totalCotText = new StringBuilder();
		StringBuilder totalText = new StringBuilder();
		final boolean[] isFirCot = {true};
		final int[] frameTime = {0};
		final int[] totalTime = {0};
//		MessageAdapter adapter = ChatManager.messageAdapter;
//		TaskPool.execute(() -> HonorHttp.sendStreamRequest(AsrRespStr, new HttpUrlConnectionHonor.StreamHandler() {
//			@Override
//			public void onStreamStop() {
//				if (endListener != null) {
//					endListener.onCancel(true);
//				}
//			}
//
//			@Override
//			public void onDataChunk(@NonNull TripHonorRes resp) {
//				if (resp.getErrorCode().equals(ErrorCode.SUC.getAlias())) {
//					var commands = resp.getChoices().getMessage().getHybridContent().getCommands();
//					var type = commands.getHead().getNamespace();
//					var body = commands.getBody();
//					var richText = body.getText();
//					if (type.equals(ContentType.THINK.getAlias())) {
//						Timber.d("消息进度onDataChunk COT %s", richText);
//						if (richText != null) {
//							if (curModule == LocalModule.TRIP_HONOR) {
//								richText = richText + "<br>";
//							}
//							totalCotText.append(richText);
//							int idx = adapter.getItemCount() - 1;
//							frameTime[0] = richText.length() * 30;
//							totalTime[0] += frameTime[0];
//							if (isFirCot[0]) {
//								Utils.replyCotContentBroadcast(richText);
//								isFirCot[0] = false;
//							} else {
//								adapter.startHonorCotAt(idx, String.valueOf(totalCotText));
//								Utils.viewScrollBottomBroadcast();
//							}
//						}
//					}
//					else if (isFirCot[0] && type.equals(ContentType.RICH_TEXT.getAlias())) {
//						Timber.d("消息进度onDataChunk RICH_TEXT %s", richText);
//						if (richText != null) {
//							totalText.append(richText);
//							Utils.replyMessageBroadcast(String.valueOf(totalText), true);
//						}
//					}
//					else if (!isFirCot[0] && type.equals(ContentType.RICH_TEXT.getAlias())) {
//						Timber.d("消息进度onDataChunk COT_SUMMARY %s", richText);
//						MessageAdapter adapter = ChatManager.messageAdapter;
//						if (richText != null) {
//							totalText.append(richText);
//							new Handler(Looper.getMainLooper()).postDelayed(() -> {
//								int idx = adapter.getItemCount() - 1;
//								adapter.setHonorCotFinish(idx, totalTime[0]);
//								adapter.startHonorTextAt(idx, String.valueOf(totalText));
//								Utils.viewScrollBottomBroadcast();
//							}, frameTime[0] + 300);
//						}
//					}
//					else if (type.equals(ContentType.CARD.getAlias())) {
//						Timber.d("消息进度onDataChunk CARD %s", body);
//						new Handler(Looper.getMainLooper()).postDelayed(() -> showHonorCard(body), frameTime[0] + 500);
//					}
//				}
//			}
//
//			@Override
//			public void onStreamComplete() {
//				String cotText = String.valueOf(totalCotText);
//				Utils.completeBroadcast();
//				HonorHttp.updateMessages(MessageRole.ASSISTANT.getAlias(), String.valueOf(totalText), "text");
//				Timber.d("消息进度onStreamComplete");
//				if (endListener != null) {
//					endListener.onCancel(true);
//				}
//				if (!TextUtils.isEmpty(cotText)) {
//					MessageDbRepository.getInstance().saveThinkMessage(cotText, String.valueOf(totalText));
//				}
//			}
//
//			@Override
//			public void onError(@NonNull String errMsg) {
//				Utils.completeBroadcast();
//				Utils.replyMessageBroadcast(errMsg, true);
//				if (endListener != null) {
//					endListener.onCancel(true);
//				}
//			}
//		}));
	}

	private void showHonorCard(BodyData body) {
		if (body.getJsCards() != null && !body.getJsCards().isEmpty()) {
			for (int i = 0; i < body.getJsCards().size(); i++) {
				CardData cardData = body.getJsCards().get(i);
				String serviceId = cardData.getServiceId();
				String templateId = cardData.getTemplateId();
				if (templateId == null) {
					if (!Objects.equals(serviceId, "")) {
						showHonorServiceCard(cardData);
					}
				}
			}
		} else if (body.getHtmls() != null && !body.getHtmls().isEmpty()) {
//			for (int i = 0; i < body.getHtmls().size(); i++) {
//				HtmlInfo cardData = body.getHtmls().get(i);
//				HtmlInfo newCardData = cardData.copy(
//						cardData.getUrl(),
//						cardData.getMode(),
//						cardData.getUrl().contains("hotel") ? 445 : cardData.getHeight(),
//						cardData.getWidth()
//				);
//				Utils.replyTravelHtmlBroadcast(newCardData);
//			}
		}
	}

	@SuppressLint({"NewApi", "LocalSuppress"})
	private void showHonorServiceCard(CardData cardData) {
		String content;
		content = URLEncoder.encode(cardData.getContent(), StandardCharsets.UTF_8);
		String type = cardData.getType();
		HtmlInfo serviceCardData = new HtmlInfo(
				"",
				0,
				0,
				0
		);
		HtmlInfo newServiceCardData = null;
//		if (type.equals(ServiceTemplateType.PLANE.getAlias())) {
//			newServiceCardData = serviceCardData.copy(
//					ServiceTemplate.PLANE.getAlias() + content,
//					0,
//					303,
//					100
//			);
//		} else if (type.equals(ServiceTemplateType.TRAIN.getAlias())) {
//			newServiceCardData = serviceCardData.copy(
//					ServiceTemplate.TRAIN.getAlias() + content,
//					0,
//					303,
//					100
//			);
//		} else if (type.equals(ServiceTemplateType.HOTEL.getAlias())) {
//			int len = content.length();
//			int height;
//			if (len < 1500) {
//				height = 150;
//			} else if (len < 2500) {
//				height = 295;
//			} else {
//				height = 442;
//			}
//			newServiceCardData = serviceCardData.copy(
//					ServiceTemplate.HOTEL.getAlias() + content,
//					0,
//					height,
//					100
//			);
//		} else if (type.equals(ServiceTemplateType.HOME.getAlias())) {
//			newServiceCardData = serviceCardData.copy(
//					ServiceTemplate.HOME.getAlias() + content,
//					0,
//					303,
//					100
//			);
//		} else if (type.equals(ServiceTemplateType.ORDER.getAlias())) {
//			newServiceCardData = serviceCardData.copy(
//					ServiceTemplate.ORDER.getAlias() + content,
//					0,
//					80,
//					100
//			);
//		} else if (type.equals(ServiceTemplateType.FOOD.getAlias())) {
//			var cardContent = cardData.getContent();
//			Utils.replyTripCardBroadcast(cardContent);
//		}
//
//		if (newServiceCardData != null) {
//			Utils.replyTravelHtmlBroadcast(newServiceCardData);
//		}
	}

	public void execAction() {
		lastExecSuc = true;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			//  HandlerLineTask.INSTANCE.start(context, AsrRespStr);
			HandlerLlm.INSTANCE.start(context, AsrRespStr);
		}
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

	private Map<String, String> getPlayData() {
		try {
			JSONObject header = result.getHeader();
			JSONObject payload = result.getPayload();
			if (header != null) {
				String name = header.optString("name");
				Map<String, String> scores = new HashMap<>();
				if (NameType.PLAY.getAlias().equals(name)) {
					assert payload != null;
					String url = Objects.requireNonNull(Objects.requireNonNull(payload.optJSONObject("audioItem")).optJSONObject("stream")).optString("url");
					String albumName = Objects.requireNonNull(payload.optJSONObject("audioItem")).optString("extension");
					scores.put("url", url);
					scores.put("albumName", albumName);
					return scores;
				}
				return null;
			}

		} catch (Exception e) {
			return null;
		}
		return null;
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

	private StreamEndListener endListener;

	public void setStreamEndListener(StreamEndListener endListener) {
		this.endListener = endListener;
	}
}