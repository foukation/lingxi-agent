package com.fxzs.lingxiagent.util.ZUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.noties.prism4j.GrammarLocator;
import io.noties.prism4j.Prism4j;
import io.noties.prism4j.languages.Prism_c;
import io.noties.prism4j.languages.Prism_clike;
import io.noties.prism4j.languages.Prism_cpp;
import io.noties.prism4j.languages.Prism_css;
import io.noties.prism4j.languages.Prism_java;
import io.noties.prism4j.languages.Prism_javascript;
import io.noties.prism4j.languages.Prism_json;
import io.noties.prism4j.languages.Prism_markup;
import io.noties.prism4j.languages.Prism_python;

import java.util.HashSet;
import java.util.Set;

/**
 * 简化的 Prism4j 语法定位器
 * 直接使用内置的语言定义，避免注解处理器的问题
 */
public class SimplePrismGrammarLocator implements GrammarLocator {
    
    private static SimplePrismGrammarLocator instance;
    
    public static SimplePrismGrammarLocator getInstance() {
        if (instance == null) {
            instance = new SimplePrismGrammarLocator();
        }
        return instance;
    }
    
    @Nullable
    @Override
    public Prism4j.Grammar grammar(@NonNull Prism4j prism4j, @NonNull String language) {
        switch (language.toLowerCase()) {
            case "clike":
                return Prism_clike.create(prism4j);
            case "c":
                return Prism_c.create(prism4j);
            case "cpp":
            case "c++":
                return Prism_cpp.create(prism4j);
            case "css":
                return Prism_css.create(prism4j);
            case "java":
                return Prism_java.create(prism4j);
            case "javascript":
            case "js":
                return Prism_javascript.create(prism4j);
            case "json":
                return Prism_json.create(prism4j);
            case "markup":
            case "html":
            case "xml":
                return Prism_markup.create(prism4j);
            case "python":
            case "py":
                return Prism_python.create(prism4j);
            default:
                // 默认返回 clike
                return Prism_clike.create(prism4j);
        }
    }
    
    @NonNull
    @Override
    public Set<String> languages() {
        Set<String> languages = new HashSet<>();
        languages.add("clike");
        languages.add("c");
        languages.add("cpp");
        languages.add("css");
        languages.add("java");
        languages.add("javascript");
        languages.add("json");
        languages.add("markup");
        languages.add("python");
        return languages;
    }
}