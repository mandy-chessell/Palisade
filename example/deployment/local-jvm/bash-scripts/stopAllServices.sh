#!/usr/bin/env bash

kill `ps -aef | grep example-rest-.*-service | grep -v grep | awk '{print $2}'`
docker stop etcd-gcr-v3.3.12
