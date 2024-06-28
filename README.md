Start docker

```
docker-compose up
```
Can also use dev container in vscode

Go to demo (java_app) container
```
docker compose exec java_app bash
```

Run the java app example

```
./run.sh
```

Run C++ app example
```
cd /app/agora_rtc_sdk/example
```
Follow steps in the this site
https://docs.agora.io/en/server-gateway/get-started/integrate-sdk?platform=linux-cpp

In the C++ app example, the agora_rtc_sdk/example/h264_pcm/sample_receive_h264_pcm.cpp has been modified to watch new h264 file is created for streaming.
