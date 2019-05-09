FROM java:8-jdk

ARG FAT_JAR_FILE

ADD target/${FAT_JAR_FILE} /app/${FAT_JAR_FILE}

