#!/usr/bin/env sh

exec java -Dspring.profiles.active=prod \
	-Dspring.aot.enabled=true -XX:SharedArchiveFile=application.jsa \
	-jar application.jar
