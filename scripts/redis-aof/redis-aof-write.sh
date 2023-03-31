#!/bin/sh

siege -d1 -t120s -c50 http://127.0.0.1:8082/messages/redis-aof/write
