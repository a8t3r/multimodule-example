#!/bin/bash

serviceName="${@%/}"
tag=$(date '+%H%M%S')

manifest="$serviceName"/build/tmp/kapt3/classes/main/META-INF/dekorate/kubernetes.yml
rm "$manifest"

./gradlew --no-build-cache :"$serviceName":dockerPush -Pgenerated-tag="$tag"
kubectl delete -f "$manifest"
kubectl apply -f "$manifest"
