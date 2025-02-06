base_url="https://huggingface.co/deepseek-ai/DeepSeek-V2-Lite/resolve/main"
models=(model-0000{1,2,3,4}-of-000004.safetensors?download=true)
echo ${models[@]}

# 循环下载
for model in "${models[@]}";
do
	echo "开始下载 $model ..."
	curl -O "$base_url/$model"
	if [ $? -eq 0 ];
	then
		echo "$model 下载成功"
	else
		echo "$model 下载失败"
	fi
done

echo "下载结束"
