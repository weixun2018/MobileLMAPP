#include <android/log.h>
#include <jni.h>
#include <iomanip>
#include <math.h>
#include <string>
#include <unistd.h>
#include "llama.h"
#include "common.h"

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("llama-android");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("llama-android")
//      }
//    }

#define TAG "llama-android.cpp"
#define LOGi(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGe(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGw(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)

jclass la_int_var;
jmethodID la_int_var_value;
jmethodID la_int_var_inc;

std::string cached_token_chars;

bool is_valid_utf8(const char * string) {
    if (!string) {
        return true;
    }

    const unsigned char * bytes = (const unsigned char *)string;
    int num;

    while (*bytes != 0x00) {
        if ((*bytes & 0x80) == 0x00) {
            // U+0000 to U+007F
            num = 1;
        } else if ((*bytes & 0xE0) == 0xC0) {
            // U+0080 to U+07FF
            num = 2;
        } else if ((*bytes & 0xF0) == 0xE0) {
            // U+0800 to U+FFFF
            num = 3;
        } else if ((*bytes & 0xF8) == 0xF0) {
            // U+10000 to U+10FFFF
            num = 4;
        } else {
            return false;
        }

        bytes += 1;
        for (int i = 1; i < num; ++i) {
            if ((*bytes & 0xC0) != 0x80) {
                return false;
            }
            bytes += 1;
        }
    }

    return true;
}

static void log_callback(ggml_log_level level, const char * fmt, void * data) {
    if (level == GGML_LOG_LEVEL_ERROR)     __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, data);
    else if (level == GGML_LOG_LEVEL_INFO) __android_log_print(ANDROID_LOG_INFO, TAG, fmt, data);
    else if (level == GGML_LOG_LEVEL_WARN) __android_log_print(ANDROID_LOG_WARN, TAG, fmt, data);
    else __android_log_print(ANDROID_LOG_DEFAULT, TAG, fmt, data);
}


extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_projectv2_LLamaAPI_load_1model(JNIEnv *env, jobject thiz, jstring filename) {
    // load dynamic backends
    ggml_backend_load_all();
    // initialize the model
    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 99;


    auto path_to_model = env->GetStringUTFChars(filename, 0);
    LOGi("Loading model from %s", path_to_model);

    auto model = llama_model_load_from_file(path_to_model, model_params);
    env->ReleaseStringUTFChars(filename, path_to_model);

    if (!model) {
        LOGe("load_model() failed");
        env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), "load_model() failed");
        return 0;
    }

    return reinterpret_cast<jlong>(model);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_projectv2_LLamaAPI_free_1model(JNIEnv *env, jobject thiz, jlong model) {
    llama_model_free(reinterpret_cast<llama_model *>(model));
}


extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_projectv2_LLamaAPI_new_1context(JNIEnv *env, jobject thiz, jlong jmodel) {
    auto model = reinterpret_cast<llama_model *>(jmodel);

    if (!model) {
        LOGe("new_context(): model cannot be null");
        env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), "Model cannot be null");
        return 0;
    }

    int n_threads = std::max(1, std::min(8, (int) sysconf(_SC_NPROCESSORS_ONLN) - 2));
    LOGi("Using %d threads", n_threads);

    llama_context_params ctx_params = llama_context_default_params();

    ctx_params.n_ctx           = 2048;
    ctx_params.n_threads       = n_threads;
    ctx_params.n_threads_batch = n_threads;

    llama_context * context = llama_new_context_with_model(model, ctx_params);

    if (!context) {
        LOGe("llama_new_context_with_model() returned null)");
        env->ThrowNew(env->FindClass("java/lang/IllegalStateException"),
                      "llama_new_context_with_model() returned null)");
        return 0;
    }

    return reinterpret_cast<jlong>(context);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_projectv2_LLamaAPI_free_1context(JNIEnv *env, jobject thiz, jlong context) {
    llama_free(reinterpret_cast<llama_context *>(context));
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_projectv2_LLamaAPI_backend_1free(JNIEnv *env, jobject thiz) {
    llama_backend_free();
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_projectv2_LLamaAPI_log_1to_1android(JNIEnv *env, jobject thiz) {
    LOGi("Setting up Android logging");
    llama_log_set(log_callback, NULL);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_projectv2_LLamaAPI_bench_1model(JNIEnv *env,
                                                 jobject thiz,
                                                 jlong context_pointer,
                                                 jlong model_pointer,
                                                 jlong batch_pointer,
                                                 jint pp,
                                                 jint tg,
                                                 jint pl,
                                                 jint nr) {
    auto pp_avg = 0.0;
    auto tg_avg = 0.0;
    auto pp_std = 0.0;
    auto tg_std = 0.0;

    const auto context = reinterpret_cast<llama_context *>(context_pointer);
    const auto model = reinterpret_cast<llama_model *>(model_pointer);
    const auto batch = reinterpret_cast<llama_batch *>(batch_pointer);

    const int n_ctx = llama_n_ctx(context);

    LOGi("n_ctx = %d", n_ctx);

    int i, j;
    int nri;
    for (nri = 0; nri < nr; nri++) {
        LOGi("Benchmark prompt processing (pp)");

        common_batch_clear(*batch);

        const int n_tokens = pp;
        for (i = 0; i < n_tokens; i++) {
            common_batch_add(*batch, 0, i, { 0 }, false);
        }

        batch->logits[batch->n_tokens - 1] = true;
        llama_kv_self_clear(context);

        const auto t_pp_start = ggml_time_us();
        if (llama_decode(context, *batch) != 0) {
            LOGi("llama_decode() failed during prompt processing");
        }
        const auto t_pp_end = ggml_time_us();

        // bench text generation

        LOGi("Benchmark text generation (tg)");

        llama_kv_self_clear(context);
        const auto t_tg_start = ggml_time_us();
        for (i = 0; i < tg; i++) {

            common_batch_clear(*batch);
            for (j = 0; j < pl; j++) {
                common_batch_add(*batch, 0, i, { j }, true);
            }

            LOGi("llama_decode() text generation: %d", i);
            if (llama_decode(context, *batch) != 0) {
                LOGi("llama_decode() failed during text generation");
            }
        }

        const auto t_tg_end = ggml_time_us();

        llama_kv_self_clear(context);

        const auto t_pp = double(t_pp_end - t_pp_start) / 1000000.0;
        const auto t_tg = double(t_tg_end - t_tg_start) / 1000000.0;

        const auto speed_pp = double(pp) / t_pp;
        const auto speed_tg = double(pl * tg) / t_tg;

        pp_avg += speed_pp;
        tg_avg += speed_tg;

        pp_std += speed_pp * speed_pp;
        tg_std += speed_tg * speed_tg;

        LOGi("pp %f t/s, tg %f t/s", speed_pp, speed_tg);
    }

    pp_avg /= double(nr);
    tg_avg /= double(nr);

    if (nr > 1) {
        pp_std = sqrt(pp_std / double(nr - 1) - pp_avg * pp_avg * double(nr) / double(nr - 1));
        tg_std = sqrt(tg_std / double(nr - 1) - tg_avg * tg_avg * double(nr) / double(nr - 1));
    } else {
        pp_std = 0;
        tg_std = 0;
    }

    char model_desc[128];
    llama_model_desc(model, model_desc, sizeof(model_desc));

    const auto model_size     = double(llama_model_size(model)) / 1024.0 / 1024.0 / 1024.0;
    const auto model_n_params = double(llama_model_n_params(model)) / 1e9;

    const auto backend    = "(Android)"; // TODO: What should this be?

    std::stringstream result;
    result << std::setprecision(2);
    result << "| model | size | params | backend | test | t/s |\n";
    result << "| --- | --- | --- | --- | --- | --- |\n";
    result << "| " << model_desc << " | " << model_size << "GiB | " << model_n_params << "B | " << backend << " | pp " << pp << " | " << pp_avg << " ± " << pp_std << " |\n";
    result << "| " << model_desc << " | " << model_size << "GiB | " << model_n_params << "B | " << backend << " | tg " << tg << " | " << tg_avg << " ± " << tg_std << " |\n";

    return env->NewStringUTF(result.str().c_str());
}


extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_projectv2_LLamaAPI_new_1batch(JNIEnv *env, jobject thiz, jint n_tokens, jint embd,
                                               jint n_seq_max) {

    // Source: Copy of llama.cpp:llama_batch_init but heap-allocated.

    llama_batch *batch = new llama_batch {
        0,
        nullptr,
        nullptr,
        nullptr,
        nullptr,
        nullptr,
        nullptr,
    };

    if (embd) {
        batch->embd = (float *) malloc(sizeof(float) * n_tokens * embd);
    } else {
        batch->token = (llama_token *) malloc(sizeof(llama_token) * n_tokens);
    }

    batch->pos      = (llama_pos *)     malloc(sizeof(llama_pos)      * n_tokens);
    batch->n_seq_id = (int32_t *)       malloc(sizeof(int32_t)        * n_tokens);
    batch->seq_id   = (llama_seq_id **) malloc(sizeof(llama_seq_id *) * n_tokens);
    for (int i = 0; i < n_tokens; ++i) {
        batch->seq_id[i] = (llama_seq_id *) malloc(sizeof(llama_seq_id) * n_seq_max);
    }
    batch->logits   = (int8_t *)        malloc(sizeof(int8_t)         * n_tokens);

    return reinterpret_cast<jlong>(batch);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_projectv2_LLamaAPI_free_1batch(JNIEnv *env, jobject thiz, jlong batch_pointer) {
    //llama_batch_free(*reinterpret_cast<llama_batch *>(batch_pointer));
    const auto batch = reinterpret_cast<llama_batch *>(batch_pointer);
    delete batch;
}


extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_projectv2_LLamaAPI_new_1sampler(JNIEnv *env, jobject thiz) {
    auto sparams = llama_sampler_chain_default_params();
//    sparams.no_perf = true;
    llama_sampler * smpl = llama_sampler_chain_init(sparams);
//    llama_sampler_chain_add(smpl, llama_sampler_init_greedy());
    llama_sampler_chain_add(smpl, llama_sampler_init_min_p(0.05f, 1));
    llama_sampler_chain_add(smpl, llama_sampler_init_temp(0.8f));
    llama_sampler_chain_add(smpl, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));
    return reinterpret_cast<jlong>(smpl);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_projectv2_LLamaAPI_free_1sampler(JNIEnv *env, jobject thiz, jlong sampler_pointer) {
    llama_sampler_free(reinterpret_cast<llama_sampler *>(sampler_pointer));
}



extern "C"
JNIEXPORT void JNICALL
Java_com_example_projectv2_LLamaAPI_backend_1init(JNIEnv *env, jobject thiz, jboolean numa) {
    llama_backend_init();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_projectv2_LLamaAPI_system_1info(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(llama_print_system_info());
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_projectv2_LLamaAPI_completion_1init(JNIEnv *env,
                                                     jobject thiz,
                                                     jlong context_pointer,
                                                     jlong batch_pointer,
                                                     jstring jtext,
                                                     jboolean format_chat,
                                                     jint n_len) {
    LOGi("Starting completion initialization");
    const auto context = reinterpret_cast<llama_context *>(context_pointer);
    const auto batch = reinterpret_cast<llama_batch *>(batch_pointer);
    const auto text = env->GetStringUTFChars(jtext, 0);
    
    LOGi("Input text: %s", text);
    LOGi("Format chat: %d", format_chat);
    LOGi("Max length: %d", n_len);
    
    cached_token_chars.clear();

    bool parse_special = (format_chat == JNI_TRUE);
    const auto tokens_list = common_tokenize(context, text, true, parse_special);

    auto n_ctx = llama_n_ctx(context);
    auto n_kv_req = tokens_list.size() + n_len;

    LOGi("n_len = %d, n_ctx = %d, n_kv_req = %d", n_len, n_ctx, n_kv_req);

    if (n_kv_req > n_ctx) {
        LOGe("错误: 上下文窗口不足! n_kv_req(%d) > n_ctx(%d), 提示token数(%zu) + 最大生成长度(%d)", 
             n_kv_req, n_ctx, tokens_list.size(), n_len);
    } else if (tokens_list.size() > n_len) {
        LOGw("警告: 提示token数(%zu)已超过最大生成长度(%d)", tokens_list.size(), n_len);
    } else {
        LOGi("提示token数: %zu, 最大生成长度: %d, 总计: %d (上下文窗口: %d)", 
             tokens_list.size(), n_len, n_kv_req, n_ctx);
    }

    for (auto id : tokens_list) {
        LOGi("token: `%s`-> %d ", common_token_to_piece(context, id).c_str(), id);
    }

    common_batch_clear(*batch);

    // evaluate the initial prompt
    for (auto i = 0; i < tokens_list.size(); i++) {
        common_batch_add(*batch, tokens_list[i], i, { 0 }, false);
    }

    // llama_decode will output logits only for the last token of the prompt
    batch->logits[batch->n_tokens - 1] = true;

    if (llama_decode(context, *batch) != 0) {
        LOGe("llama_decode() failed");
    }

    env->ReleaseStringUTFChars(jtext, text);

    return batch->n_tokens;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_projectv2_LLamaAPI_completion_1loop(JNIEnv *env, jobject thiz,
                                                     jlong context_pointer, jlong batch_pointer, jlong sampler_pointer,
                                                     jint n_len, jobject intvar_ncur) {

    const auto context = reinterpret_cast<llama_context *>(context_pointer);
    const auto batch = reinterpret_cast<llama_batch *>(batch_pointer);
    const auto sampler = reinterpret_cast<llama_sampler *>(sampler_pointer);
    const auto model = llama_get_model(context);
    const auto vocab = llama_model_get_vocab(model);
//    const bool is_first = llama_kv_self_used_cells(context) == 0;
    // 获取IntVar Java对象
    if (!la_int_var) la_int_var = env->GetObjectClass(intvar_ncur);
    if (!la_int_var_value) la_int_var_value = env->GetMethodID(la_int_var, "getValue", "()I");
    if (!la_int_var_inc) la_int_var_inc = env->GetMethodID(la_int_var, "inc", "()V");

    // 采样下一个token
    const auto new_token_id = llama_sampler_sample(sampler, context, -1);

    // 检测是否是结束标记
    if (llama_vocab_is_eog(vocab, new_token_id)) {
        LOGi("检测到EOG标记，结束生成");
        return nullptr;
    }

    // 将token ID转换为字符串
    auto new_token_chars = common_token_to_piece(context, new_token_id);
    cached_token_chars += new_token_chars;

    // 确保是有效的UTF-8序列
    jstring new_token = nullptr;
    if (is_valid_utf8(cached_token_chars.c_str())) {
        new_token = env->NewStringUTF(cached_token_chars.c_str());
        cached_token_chars.clear();
    } else {
        // 如果不是有效的UTF-8，返回空字符串并保留缓存
        new_token = env->NewStringUTF("");
    }

    // 增加计数器
    env->CallVoidMethod(intvar_ncur, la_int_var_inc);
    
    // 获取当前位置用于日志和准备batch
    const auto n_cur = env->CallIntMethod(intvar_ncur, la_int_var_value);

    // 准备下一个batch
    common_batch_clear(*batch);
    common_batch_add(*batch, new_token_id, n_cur, { 0 }, true);

    // 解码
    if (llama_decode(context, *batch) != 0) {
        LOGe("llama_decode() 失败");
    }

    return new_token;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_projectv2_LLamaAPI_kv_1cache_1clear(JNIEnv *env, jobject thiz, jlong context) {
    llama_kv_self_clear(reinterpret_cast<llama_context *>(context));
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_projectv2_LLamaAPI_get_1kv_1cache_1used(JNIEnv *env, jobject thiz, jlong context_pointer) {
    const auto context = reinterpret_cast<llama_context *>(context_pointer);
    return llama_kv_self_used_cells(context);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_projectv2_LLamaAPI_get_1context_1size(JNIEnv *env, jobject thiz, jlong context_pointer) {
    const auto context = reinterpret_cast<llama_context *>(context_pointer);
    return llama_n_ctx(context);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_projectv2_LLamaAPI_incremental_1chat_1completion(JNIEnv *env,
                                                                 jobject thiz,
                                                                 jlong context_pointer,
                                                                 jlong batch_pointer,
                                                                 jlong model_pointer,
                                                                 jobject new_message,
                                                                 jint n_len) {
    LOGi("开始增量聊天处理");
    const auto context = reinterpret_cast<llama_context *>(context_pointer);
    const auto batch = reinterpret_cast<llama_batch *>(batch_pointer);
    const auto model = reinterpret_cast<llama_model *>(model_pointer);
    
    // 检查KV缓存状态
    const int n_ctx_used = llama_kv_self_used_cells(context);
    const int n_ctx = llama_n_ctx(context);
    
    LOGi("KV缓存状态: %d/%d 单元已使用", n_ctx_used, n_ctx);
    
    if (n_ctx_used == 0) {
        LOGi("KV缓存为空，需要完整处理");
        return -1;
    }
    
    // 获取ChatMessage类和方法
    jclass chatMessageClass = env->FindClass("com/example/projectv2/LLamaAPI$ChatMessage");
    if (!chatMessageClass) {
        LOGe("找不到ChatMessage类");
        return -1;
    }
    
    jfieldID roleField = env->GetFieldID(chatMessageClass, "role", "Ljava/lang/String;");
    jfieldID contentField = env->GetFieldID(chatMessageClass, "content", "Ljava/lang/String;");
    if (!roleField || !contentField) {
        LOGe("找不到ChatMessage字段");
        return -1;
    }
    
    // 获取新消息信息
    jstring jrole = (jstring)env->GetObjectField(new_message, roleField);
    jstring jcontent = (jstring)env->GetObjectField(new_message, contentField);
    if (!jrole || !jcontent) {
        LOGe("消息字段为空");
        return -1;
    }
    
    const char* role = env->GetStringUTFChars(jrole, nullptr);
    const char* content = env->GetStringUTFChars(jcontent, nullptr);
    if (!role || !content) {
        LOGe("无法获取消息内容");
        if (role) env->ReleaseStringUTFChars(jrole, role);
        if (content) env->ReleaseStringUTFChars(jcontent, content);
        return -1;
    }
    
    LOGi("增量处理消息 - 角色: %s, 内容长度: %zu", role, strlen(content));
    
    // 获取聊天模板
    const char* tmpl = llama_model_chat_template(model, nullptr);
    if (!tmpl) {
        LOGe("无法获取聊天模板");
        env->ReleaseStringUTFChars(jrole, role);
        env->ReleaseStringUTFChars(jcontent, content);
        return -1;
    }
    
    // 准备提示
    std::string prompt;
    
    // 根据角色确定处理方式
    bool is_user_message = (strcmp(role, "user") == 0);
    
    if (is_user_message) {
        // 使用RAII管理临时消息数组
        struct MessageCleanup {
            std::vector<llama_chat_message>& messages;
            ~MessageCleanup() {
                for (auto& msg : messages) {
                    if (msg.role && msg.role != "assistant") {
                        free((void*)msg.role);
                    }
                    if (msg.content) {
                        free((void*)msg.content);
                    }
                }
            }
        };
        
        // 创建临时消息数组，确保所有字符串都是动态分配的
        std::vector<llama_chat_message> temp_messages;
        
        // 用户消息
        char* user_role = strdup(role);
        char* user_content = strdup(content);
        if (!user_role || !user_content) {
            LOGe("内存分配失败");
            if (user_role) free(user_role);
            if (user_content) free(user_content);
            env->ReleaseStringUTFChars(jrole, role);
            env->ReleaseStringUTFChars(jcontent, content);
            return -1;
        }
        temp_messages.push_back({user_role, user_content});
        
        // 助手消息
        char* assistant_role = strdup("assistant");
        char* assistant_content = strdup("");
        if (!assistant_role || !assistant_content) {
            LOGe("内存分配失败");
            if (assistant_role) free(assistant_role);
            if (assistant_content) free(assistant_content);
            env->ReleaseStringUTFChars(jrole, role);
            env->ReleaseStringUTFChars(jcontent, content);
            return -1;
        }
        temp_messages.push_back({assistant_role, assistant_content});
        
        MessageCleanup cleanup{temp_messages};
        
        // 准备格式化缓冲区
        std::vector<char> formatted(4096);
        
        // 使用标准API应用模板
        int format_len = llama_chat_apply_template(tmpl, temp_messages.data(), temp_messages.size(), 
                                                  true, formatted.data(), formatted.size());
        
        if (format_len > (int)formatted.size()) {
            formatted.resize(format_len + 1);
            format_len = llama_chat_apply_template(tmpl, temp_messages.data(), temp_messages.size(), 
                                                 true, formatted.data(), formatted.size());
        }
        
        if (format_len < 0) {
            LOGe("应用模板失败");
            env->ReleaseStringUTFChars(jrole, role);
            env->ReleaseStringUTFChars(jcontent, content);
            return -1;
        }
        
        // 解析格式化的字符串，找到用户消息和助手回复的分界点
        std::string formatted_str(formatted.data(), format_len);
        
        // 找到最后一个助手标记的位置
        size_t assistant_pos = formatted_str.rfind("assistant");
        
        if (assistant_pos != std::string::npos) {
            // 提取用户消息+助手前缀
            prompt = formatted_str.substr(0, assistant_pos + 9); // "assistant"长度
            LOGi("提取用户消息+助手前缀，长度: %zu", prompt.length());
        } else {
            // 如果找不到助手标记，使用整个格式化字符串
            prompt = formatted_str;
            LOGi("未找到assistant标记，使用完整文本");
        }
    } else {
        // 非用户消息则直接使用内容
        prompt = content;
    }
    
    // 标记化增量提示
    cached_token_chars.clear();
    const auto tokens_list = common_tokenize(context, prompt.c_str(), true, false);
    
    // 检查KV缓存空间是否足够
    size_t n_kv_req = n_ctx_used + tokens_list.size() + n_len;
    
    LOGi("KV缓存需求: 已用(%d) + 新token(%zu) + 生成长度(%d) = %zu/%d", 
         n_ctx_used, tokens_list.size(), n_len, n_kv_req, n_ctx);
    
    // 如果空间不足，返回错误
    if (n_kv_req > (size_t)n_ctx) {
        LOGe("KV缓存空间不足! %zu > %d，回退到完整处理", n_kv_req, n_ctx);
        env->ReleaseStringUTFChars(jrole, role);
        env->ReleaseStringUTFChars(jcontent, content);
        return -2; // 特殊错误码表示空间不足
    }
    
    // 准备batch
    common_batch_clear(*batch);
    
    // 从现有KV缓存的末尾开始添加
    const int start_pos = n_ctx_used;
    
    // 将新token添加到batch
    for (size_t i = 0; i < tokens_list.size(); i++) {
        common_batch_add(*batch, tokens_list[i], start_pos + i, { 0 }, false);
    }
    
    // 只为最后一个token生成logits
    if (batch->n_tokens > 0) {
        batch->logits[batch->n_tokens - 1] = true;
    }
    
    // 处理增量batch
    LOGi("处理增量batch中，%zu 个token从位置 %d 开始", tokens_list.size(), start_pos);
    if (llama_decode(context, *batch) != 0) {
        LOGe("llama_decode() 失败");
        env->ReleaseStringUTFChars(jrole, role);
        env->ReleaseStringUTFChars(jcontent, content);
        return -1;
    }
    
    // 释放资源
    env->ReleaseStringUTFChars(jrole, role);
    env->ReleaseStringUTFChars(jcontent, content);
    
    // 返回新的开始位置供生成使用
    int new_start = start_pos + tokens_list.size();
    LOGi("增量处理成功，新起点: %d", new_start);
    return new_start;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_projectv2_LLamaAPI_chat_1completion_1init(JNIEnv *env,
                                                           jobject thiz,
                                                           jlong context_pointer,
                                                           jlong batch_pointer,
                                                           jlong model_pointer,
                                                           jobject messages_list,
                                                           jint n_len) {
    LOGi("Starting chat completion initialization");
    const auto context = reinterpret_cast<llama_context *>(context_pointer);
    const auto batch = reinterpret_cast<llama_batch *>(batch_pointer);
    const auto model = reinterpret_cast<llama_model *>(model_pointer);

    // 获取ChatMessage类和相关方法
    jclass chatMessageClass = env->FindClass("com/example/projectv2/LLamaAPI$ChatMessage");
    jfieldID roleField = env->GetFieldID(chatMessageClass, "role", "Ljava/lang/String;");
    jfieldID contentField = env->GetFieldID(chatMessageClass, "content", "Ljava/lang/String;");

    // 获取List对象的方法
    jclass listClass = env->GetObjectClass(messages_list);
    jmethodID sizeMethod = env->GetMethodID(listClass, "size", "()I");
    jmethodID getMethod = env->GetMethodID(listClass, "get", "(I)Ljava/lang/Object;");

    // 获取消息数量
    jint messagesSize = env->CallIntMethod(messages_list, sizeMethod);
    LOGi("Chat history size: %d", messagesSize);

    // 创建llama_chat_message数组
    std::vector<llama_chat_message> llm_messages;
    for (int i = 0; i < messagesSize; i++) {
        jobject message = env->CallObjectMethod(messages_list, getMethod, i);
        jstring jrole = (jstring)env->GetObjectField(message, roleField);
        jstring jcontent = (jstring)env->GetObjectField(message, contentField);

        const char* role = env->GetStringUTFChars(jrole, nullptr);
        const char* content = env->GetStringUTFChars(jcontent, nullptr);

        llm_messages.push_back({strdup(role), strdup(content)});

        env->ReleaseStringUTFChars(jrole, role);
        env->ReleaseStringUTFChars(jcontent, content);
        env->DeleteLocalRef(message);
        env->DeleteLocalRef(jrole);
        env->DeleteLocalRef(jcontent);
    }

    // 获取聊天模板
    const char* tmpl = llama_model_chat_template(model, nullptr);
    LOGi("Using chat template: %s", tmpl ? "yes" : "no template available");

    // 分配格式化缓冲区
    std::vector<char> formatted(llama_n_ctx(context));

    // 应用聊天模板
    int new_len = llama_chat_apply_template(tmpl, llm_messages.data(), llm_messages.size(),
                                            true, formatted.data(), formatted.size());
    if (new_len > (int)formatted.size()) {
        formatted.resize(new_len);
        new_len = llama_chat_apply_template(tmpl, llm_messages.data(), llm_messages.size(),
                                            true, formatted.data(), formatted.size());
    }

    if (new_len < 0) {
        LOGe("Failed to apply chat template");
        return 0;
    }

    std::string prompt(formatted.data(), new_len);
    LOGi("Formatted prompt: %s", prompt.c_str());

    cached_token_chars.clear();

    // 标记化格式化的提示
    const auto tokens_list = common_tokenize(context, prompt.c_str(), true, false);

    auto n_ctx = llama_n_ctx(context);
    auto n_kv_req = tokens_list.size() + n_len;

    LOGi("n_len = %d, n_ctx = %d, n_kv_req = %d", n_len, n_ctx, n_kv_req);

    if (n_kv_req > n_ctx) {
        LOGe("错误: 上下文窗口不足! n_kv_req(%d) > n_ctx(%d), 提示token数(%zu) + 最大生成长度(%d)", 
             n_kv_req, n_ctx, tokens_list.size(), n_len);
    } else if (tokens_list.size() > n_len) {
        LOGw("警告: 提示token数(%zu)已超过最大生成长度(%d)", tokens_list.size(), n_len);
    } else {
        LOGi("提示token数: %zu, 最大生成长度: %d, 总计: %d (上下文窗口: %d)", 
             tokens_list.size(), n_len, n_kv_req, n_ctx);
    }

    common_batch_clear(*batch);

    // 评估初始提示
    for (auto i = 0; i < tokens_list.size(); i++) {
        common_batch_add(*batch, tokens_list[i], i, { 0 }, false);
    }

    // 只为提示的最后一个标记输出logits
    batch->logits[batch->n_tokens - 1] = true;

    if (llama_decode(context, *batch) != 0) {
        LOGe("llama_decode() failed");
    }

    // 释放分配的内存
    for (auto& msg : llm_messages) {
        free(const_cast<char*>(msg.role));
        free(const_cast<char*>(msg.content));
    }

    return batch->n_tokens;
}