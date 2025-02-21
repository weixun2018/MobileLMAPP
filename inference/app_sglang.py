import openai
client = openai.Client(
    base_url="http://127.0.0.1:30000/v1", api_key="EMPTY")

stream = client.chat.completions.create(
    model="default",
    messages=[
        {"role": "system", "content": "You are a helpful AI assistant"},
        {"role": "user", "content": "我担心毕业后找不到理想的工作，该如何应对这种不确定性？"},
    ],
    stream=True,
)
st = time.time()
for chunk in stream:
  if chunk.choices[0].delta.content is not None:
    print(chunk.choices[0].delta.content, end="")
ed = time.time()
print(f"\n\n共计{ed - st:.2f}s")