#include <android/log.h>
#include <jni.h>
#include <iomanip>
#include <math.h>
#include <string>
#include <unistd.h>
#include "llama.h"
#include "common.h"
#include <vector>
#include <unordered_set>
#include <sys/sysinfo.h>

// 全局变量跟踪聊天历史的格式化长度
static int prev_formatted_len = 0;

// 全局消息历史 - 参考示例代码
static std::vector<std::string> global_str_storage;  // 存储字符串内容
static std::vector<llama_chat_message> global_messages;  // 存储消息历史

// 添加消息到历史
static void add_message_to_history(const char* role, const char* content) {
    // 存储字符串
    global_str_storage.push_back(role);
    global_str_storage.push_back(content);
    
    // 添加到消息历史
    size_t role_idx = global_str_storage.size() - 2;
    size_t content_idx = global_str_storage.size() - 1;
    global_messages.push_back({
        global_str_storage[role_idx].c_str(), 
        global_str_storage[content_idx].c_str()
    });
}

// 清除消息历史
static void clear_message_history() {
    global_str_storage.clear();
    global_messages.clear();
    prev_formatted_len = 0;
}

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

static void log_callback(ggml_log_level level, const char * fmt, void * data) {
    if (level == GGML_LOG_LEVEL_ERROR)     __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, data);
    else if (level == GGML_LOG_LEVEL_INFO) __android_log_print(ANDROID_LOG_INFO, TAG, fmt, data);
    else if (level == GGML_LOG_LEVEL_WARN) __android_log_print(ANDROID_LOG_WARN, TAG, fmt, data);
    else __android_log_print(ANDROID_LOG_DEFAULT, TAG, fmt, data);
}

// 优化的标记化函数，参考simple-chat.cpp
std::vector<llama_token> tokenize_optimized(llama_context* ctx, const char* text, bool add_bos) {
    const auto model = llama_get_model(ctx);
    const auto vocab = llama_model_get_vocab(model);

    // 使用示例代码的方式获取token数量
    const int n_tokens = -llama_tokenize(vocab, text, strlen(text), NULL, 0, add_bos, true);

    if (n_tokens <= 0) {
        LOGe("标记化失败: 无法确定token数量");
        return {};
    }

    // 分配精确内存
    std::vector<llama_token> tokens(n_tokens);

    // 实际标记化
    if (llama_tokenize(vocab, text, strlen(text), tokens.data(), tokens.size(), add_bos, true) < 0) {
        LOGe("标记化失败: 无法转换文本到tokens");
        return {};
    }

    return tokens;
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

    ctx_params.n_ctx           = 4096;
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
Java_com_example_projectv2_LLamaAPI_new_1sampler(JNIEnv *env, jobject thiz, jfloat temp) {
    auto sparams = llama_sampler_chain_default_params();
    llama_sampler * smpl = llama_sampler_chain_init(sparams);
    
    LOGi("创建采样器 - 温度: %.2f", temp);
    
    // 使用与simple-chat.cpp相同的配置
    llama_sampler_chain_add(smpl, llama_sampler_init_min_p(0.05f, 1));
    
    if (temp <= 0.0f) {
        // 温度为0时使用贪婪搜索
        LOGi("启用贪婪采样策略");
        llama_sampler_chain_add(smpl, llama_sampler_init_greedy());
    } else {
        // 使用温度采样
        LOGi("启用温度采样策略: %.2f", temp);
        llama_sampler_chain_add(smpl, llama_sampler_init_temp(temp));
    }
    
    // 添加分布采样
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
    ggml_backend_load_all();
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
    const auto context = reinterpret_cast<llama_context *>(context_pointer);
    const auto batch = reinterpret_cast<llama_batch *>(batch_pointer);
    const auto model = llama_get_model(context);
    
    auto text = env->GetStringUTFChars(jtext, 0);
    LOGi("初始化补全，文本长度: %zu", strlen(text));
    
    // 使用聊天模板处理
    // 获取聊天模板
    const char* tmpl = llama_model_chat_template(model, nullptr);
    LOGi("使用聊天模板: %s", tmpl ? "是" : "否");
    
    // 创建聊天消息
    std::vector<std::string> str_storage;
    std::vector<llama_chat_message> messages;
    
    // 将输入文本作为user消息
    str_storage.push_back("user");
    str_storage.push_back(text);
    messages.push_back({str_storage[0].c_str(), str_storage[1].c_str()});
    
    // 添加空的assistant回复
    messages.push_back({"assistant", ""});
    
    // 应用模板
    std::vector<char> formatted(4096, 0);
    int format_len = llama_chat_apply_template(tmpl, messages.data(), messages.size(),
                                            true, formatted.data(), formatted.size());
    
    if (format_len > (int)formatted.size()) {
        formatted.resize(format_len + 1, 0);
        format_len = llama_chat_apply_template(tmpl, messages.data(), messages.size(),
                                            true, formatted.data(), formatted.size());
    }
    
    if (format_len < 0) {
        LOGe("应用聊天模板失败");
        env->ReleaseStringUTFChars(jtext, text);
        return 0;
    }
    
    std::string prompt(formatted.data(), format_len);
    LOGi("格式化后提示: %s", prompt.c_str());
    
    // 使用优化的标记化
    const auto tokens_list = tokenize_optimized(context, prompt.c_str(), true);
    
    // 检查标记化是否成功
    if (tokens_list.empty()) {
        LOGe("标记化失败: 无法生成tokens");
        env->ReleaseStringUTFChars(jtext, text);
        return 0;
    }
    
    LOGi("token化完成，共 %zu 个token", tokens_list.size());
    
    auto n_ctx = llama_n_ctx(context);
    auto n_kv_req = tokens_list.size() + n_len;
    
    LOGi("n_len = %d, n_ctx = %d, n_kv_req = %d", n_len, n_ctx, n_kv_req);
    
    if (n_kv_req > n_ctx) {
        LOGe("错误: 上下文窗口不足! n_kv_req(%d) > n_ctx(%d), 提示token数(%zu) + 最大生成长度(%d)", 
             n_kv_req, n_ctx, tokens_list.size(), n_len);
    } else {
        LOGi("提示token数: %zu, 最大生成长度: %d, 总计: %d (上下文窗口: %d)", 
             tokens_list.size(), n_len, n_kv_req, n_ctx);
    }
    
    // 使用llama_batch_get_one，参考simple-chat.cpp的实现
    *batch = llama_batch_get_one(const_cast<llama_token*>(tokens_list.data()), tokens_list.size());
    
    // 检查是否有足够的上下文空间
    int n_ctx_used = llama_kv_self_used_cells(context);
    if (n_ctx_used + batch->n_tokens > n_ctx) {
        LOGe("上下文大小超出限制：%d + %d > %d", n_ctx_used, batch->n_tokens, n_ctx);
        env->ReleaseStringUTFChars(jtext, text);
        return 0;
    }
    
    // 解码提示 - 不手动设置logits标志
    if (llama_decode(context, *batch) != 0) {
        LOGe("llama_decode() 失败");
        env->ReleaseStringUTFChars(jtext, text);
        return 0;
    }
    
    // 更新格式化长度，使用格式化后的长度
    prev_formatted_len = format_len;
    LOGi("完成初始化，设置格式化长度: %d", prev_formatted_len);
    
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
    
    // 获取IntVar Java对象
    if (!la_int_var) la_int_var = env->GetObjectClass(intvar_ncur);
    if (!la_int_var_value) la_int_var_value = env->GetMethodID(la_int_var, "getValue", "()I");
    if (!la_int_var_inc) la_int_var_inc = env->GetMethodID(la_int_var, "inc", "()V");

    // 采样下一个token
    const auto new_token_id = llama_sampler_sample(sampler, context, -1);
    LOGi("采样得到token ID: %d", new_token_id);

    // 直接使用llama_vocab_is_eog检测结束标记，与simple-chat.cpp保持一致
    if (llama_vocab_is_eog(vocab, new_token_id)) {
        LOGi("检测到EOG标记: %d，结束生成", new_token_id);
        
        // 在生成结束时，更新消息历史中助手的回复内容
        // 如果有累积的助手回复，保存到历史
        if (global_messages.size() >= 2) {
            // 获取聊天模板更新格式化长度
            const auto model = llama_get_model(context);
            const char* tmpl = llama_model_chat_template(model, nullptr);
            if (tmpl) {
                prev_formatted_len = llama_chat_apply_template(tmpl, global_messages.data(), global_messages.size(),
                                                          false, nullptr, 0);
                LOGi("生成结束，更新格式化长度: %d", prev_formatted_len);
            }
        }
        
        return nullptr;
    }

    // 采用simple-chat.cpp的方式处理token
    char buf[256];
    int n = llama_token_to_piece(vocab, new_token_id, buf, sizeof(buf), 0, true);
    if (n < 0) {
        LOGe("转换token失败");
        return env->NewStringUTF("");
    }
    
    // 确保字符串长度精确控制，使用正确长度构造字符串
    std::string piece(buf, n);
    LOGi("生成token: %d -> '%s'", new_token_id, piece.c_str());
    
    // 尝试更新全局历史中的助手回复
    if (global_messages.size() >= 2) {
        // 最后一条消息应该是助手的空回复，我们需要逐步添加内容
        size_t last_index = global_messages.size() - 1;
        
        // 获取当前内容
        std::string current_content = global_str_storage[last_index * 2 + 1];
        
        // 添加新token
        current_content += piece;
        
        // 更新内容
        global_str_storage[last_index * 2 + 1] = current_content;
        
        // 更新指针
        global_messages[last_index].content = global_str_storage[last_index * 2 + 1].c_str();
    }

    // 增加计数器
    env->CallVoidMethod(intvar_ncur, la_int_var_inc);

    // 使用llama_batch_get_one创建单token的batch
    *batch = llama_batch_get_one(const_cast<llama_token*>(&new_token_id), 1);

    // 解码 - 不手动设置logits标志
    if (llama_decode(context, *batch) != 0) {
        LOGe("llama_decode() 失败");
        return nullptr;
    }

    return env->NewStringUTF(piece.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_projectv2_LLamaAPI_kv_1cache_1clear(JNIEnv *env, jobject thiz, jlong context) {
    llama_kv_self_clear(reinterpret_cast<llama_context *>(context));
    // 重置格式化长度
    prev_formatted_len = 0;
    // 清除消息历史
    clear_message_history();
    LOGi("KV缓存和聊天历史已重置");
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
JNIEXPORT jboolean JNICALL
Java_com_example_projectv2_LLamaAPI_can_1fit_1in_1kv_1cache(JNIEnv *env, jobject thiz,
                                                           jlong context_pointer,
                                                           jint n_tokens_to_add) {
    const auto context = reinterpret_cast<llama_context *>(context_pointer);
    int n_ctx = llama_n_ctx(context);
    int n_ctx_used = llama_kv_self_used_cells(context);
    
    LOGi("KV缓存状态: %d/%d，需要添加 %d 个token", n_ctx_used, n_ctx, n_tokens_to_add);
    
    // 简单检查是否有足够空间
    return (n_ctx_used + n_tokens_to_add <= n_ctx) ? JNI_TRUE : JNI_FALSE;
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
    
    if (n_ctx_used == 0 || global_messages.empty()) {
        LOGi("KV缓存为空或消息历史为空，需要完整处理");
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
    
    // 保存原始格式化长度
    int old_formatted_len = prev_formatted_len;
    
    // 添加新消息到历史
    add_message_to_history(role, content);
    
    // 准备空的助手回复 - 参考示例代码
    add_message_to_history("assistant", "");
    
    // 分配格式化缓冲区并初始化为0
    std::vector<char> formatted(8192, 0);  // 使用较大的初始大小
    
    // 应用模板到完整历史
    int format_len = llama_chat_apply_template(tmpl, global_messages.data(), global_messages.size(), 
                                              true, formatted.data(), formatted.size());
    
    if (format_len > (int)formatted.size()) {
        formatted.resize(format_len + 1, 0);  // 增加1字节用于null终止符
        format_len = llama_chat_apply_template(tmpl, global_messages.data(), global_messages.size(), 
                                             true, formatted.data(), formatted.size());
    }
    
    if (format_len < 0) {
        LOGe("应用模板失败");
        env->ReleaseStringUTFChars(jrole, role);
        env->ReleaseStringUTFChars(jcontent, content);
        return -1;
    }
    
    // 关键部分：使用官方提供的示例代码的方式，只获取新增部分
    std::string prompt;
    if (old_formatted_len > 0 && old_formatted_len < format_len) {
        // 只用新增的部分作为提示
        prompt = std::string(formatted.data() + old_formatted_len, format_len - old_formatted_len);
        LOGi("只使用新增部分，长度: %zu", prompt.length());
    } else {
        // 出现问题时，使用完整提示
        prompt = std::string(formatted.data(), format_len);
        LOGi("使用完整提示，长度: %zu", prompt.length());
    }
    
    // 更新格式化长度 - 参考示例代码
    // 注意：这里不更新prev_formatted_len，因为我们还未添加助手回复
    
    // 标记化增量提示
    const auto tokens_list = tokenize_optimized(context, prompt.c_str(), false);  // 不添加BOS
    
    // 检查标记化是否成功
    if (tokens_list.empty()) {
        LOGe("标记化失败: 无法生成tokens");
        env->ReleaseStringUTFChars(jrole, role);
        env->ReleaseStringUTFChars(jcontent, content);
        return -1;
    }
    
    // 检查KV缓存空间是否足够
    size_t n_kv_req = n_ctx_used + tokens_list.size() + n_len;
    
    LOGi("KV缓存需求: 已用(%d) + 新token(%zu) + 生成长度(%d) = %zu/%d", 
         n_ctx_used, tokens_list.size(), n_len, n_kv_req, n_ctx);
    
    // 如果空间不足，返回错误
    if (n_kv_req > (size_t)n_ctx) {
        LOGe("KV缓存空间不足! %zu > %d，回退到完整处理", n_kv_req, n_ctx);
        env->ReleaseStringUTFChars(jrole, role);
        env->ReleaseStringUTFChars(jcontent, content);
        
        // 清理刚才添加的消息
        if (global_messages.size() >= 2) {
            global_messages.pop_back(); // 移除空的assistant消息
            global_messages.pop_back(); // 移除用户消息
        }
        
        return -2; // 特殊错误码表示空间不足
    }
    
    // 使用llama_batch_get_one，参考simple-chat.cpp的实现
    *batch = llama_batch_get_one(const_cast<llama_token*>(tokens_list.data()), tokens_list.size());
    
    // 处理增量batch - 不手动设置logits标志
    LOGi("处理增量batch中，%zu 个token", tokens_list.size());
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
    int new_start = n_ctx_used + tokens_list.size();
    
    // 打印详细的KV缓存状态和增量处理结果
    float kv_usage_percent = (float)new_start / n_ctx * 100.0f;
    LOGi("增量处理成功，新起点: %d，原使用量: %d，新增token: %zu，最终使用量: %d (%.1f%%)",
         new_start, n_ctx_used, tokens_list.size(), new_start, kv_usage_percent);
    
    // 确保KV缓存中有数据
    if (new_start <= 0) {
        LOGe("错误：增量处理后KV缓存为空或无效，返回-1触发完整处理");
        return -1;
    }
    
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

    // 在完整处理开始时重置历史
    clear_message_history();
    LOGi("重置聊天历史");

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

    // 加载所有消息到全局历史
    for (int i = 0; i < messagesSize; i++) {
        jobject message = env->CallObjectMethod(messages_list, getMethod, i);
        jstring jrole = (jstring)env->GetObjectField(message, roleField);
        jstring jcontent = (jstring)env->GetObjectField(message, contentField);

        const char* role = env->GetStringUTFChars(jrole, nullptr);
        const char* content = env->GetStringUTFChars(jcontent, nullptr);

        // 添加到全局历史
        add_message_to_history(role, content);
        
        // 释放资源
        env->ReleaseStringUTFChars(jrole, role);
        env->ReleaseStringUTFChars(jcontent, content);
        env->DeleteLocalRef(message);
        env->DeleteLocalRef(jrole);
        env->DeleteLocalRef(jcontent);
    }

    // 获取聊天模板
    const char* tmpl = llama_model_chat_template(model, nullptr);
    LOGi("Using chat template: %s", tmpl ? "yes" : "no template available");

    // 分配格式化缓冲区并初始化为0
    std::vector<char> formatted(4096, 0);  // 使用较小的初始大小并初始化为0

    // 应用聊天模板到全局历史
    int format_len = llama_chat_apply_template(tmpl, global_messages.data(), global_messages.size(),
                                            true, formatted.data(), formatted.size());
    if (format_len > (int)formatted.size()) {
        formatted.resize(format_len + 1, 0);  // 增加1字节用于null终止符，并初始化为0
        format_len = llama_chat_apply_template(tmpl, global_messages.data(), global_messages.size(),
                                            true, formatted.data(), formatted.size());
    }

    if (format_len < 0) {
        LOGe("Failed to apply chat template");
        return 0;
    }

    std::string prompt(formatted.data(), format_len);
    LOGi("Formatted prompt: %s", prompt.c_str());

    // 使用优化的标记化函数
    const auto tokens_list = tokenize_optimized(context, prompt.c_str(), true);
    
    // 检查标记化是否成功
    if (tokens_list.empty()) {
        LOGe("标记化失败: 无法生成tokens");
        return 0;
    }

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

    // 使用llama_batch_get_one，参考simple-chat.cpp的实现
    *batch = llama_batch_get_one(const_cast<llama_token*>(tokens_list.data()), tokens_list.size());

    // 检查是否有足够的上下文空间
    int n_ctx_used = llama_kv_self_used_cells(context);
    if (n_ctx_used + batch->n_tokens > n_ctx) {
        LOGe("上下文大小超出限制：%d + %d > %d", n_ctx_used, batch->n_tokens, n_ctx);
        return 0;
    }
    
    // 解码 - 不手动设置logits标志
    if (llama_decode(context, *batch) != 0) {
        LOGe("llama_decode() failed");
        return 0;
    }

    // 更新格式化长度，用于下一次增量处理
    // 直接使用示例代码方式计算pre_len
    prev_formatted_len = llama_chat_apply_template(tmpl, global_messages.data(), global_messages.size(),
                                               false, nullptr, 0);
    LOGi("完成初始化，设置格式化长度: %d", prev_formatted_len);

    return batch->n_tokens;
}