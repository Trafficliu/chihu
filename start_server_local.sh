CHIHU_ROOT=~/dev-workspace/chihu/
cd ${CHIHU_ROOT}/server
gradle bootRun -Penv=local \
-Pmysql-address=localhost:3306 \
-Pmysql-username=root \
-Pmysql-password=chihu_dev_db \
-Pwhitelist-ip-enable=false \
-Pwhitelist-ip-list=127.0.0.1,::1 \
