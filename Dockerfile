# Import base image
FROM openjdk:8u212-jre

# Create log file directory and set permission
RUN groupadd -r gwas-rest-api && useradd -r --create-home -g gwas-rest-api gwas-rest-api
RUN if [ ! -d /var/log/gwas/ ];then mkdir /var/log/gwas/;fi
RUN chown -R gwas-rest-api:gwas-rest-api /var/log/gwas

# Move project artifact
ADD target/gwas-rest-api-*.jar /home/gwas-rest-api/
USER gwas-rest-api

# Launch application server
ENTRYPOINT exec $JAVA_HOME/bin/java $XMX $XMS -jar -Dspring.profiles.active=$ENVIRONMENT -Dspring.datasource.username=$DB_USER -Dspring.datasource.password=$DB_PASSWORD /home/gwas-rest-api/gwas-rest-api-*.jar
