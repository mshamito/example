# Пример использования КриптоПро JCP в Spring
В данном примере реализован REST API сервис для создания CMS подписи и шифрования

# Предустановка
## java 8 only
* установить JCP/JCSP в java 8
* скопировать файлы bc*.jar в JRE/lib/ext
* скопировать jar-ники JCP/JCSP в PROJECT_DIR/libs
* скопировать файлы bc*.jar из папки dependencies в PROJECT_DIR/libs


## java 10+
* скопировать jar-ники JCP/JCSP в PROJECT_DIR/libs
* скопировать файлы bc*.jar из папки dependencies в PROJECT_DIR/libs

# Настройка
указать считыватель, алиас (имя контейнера) и пин код в application.yml

# Сборка
```shell
./gradlew bootJar
```

# Запуск
```shell
./gradlew bootRun
```

# Использование
подпись
```shell
curl http://localhost:8080/sign -F data=@FILE [ -F tsp=http://TSP_SERVER/tsp/tsp.srf -F type=TYPE -F detached=true ]
```
где  
FILE - файл который необходимо подписать  
TSP_SERVER - адрес сервера службы TSP  
TYPE - тип подписи. допустимые значения: BES / T / XLT1 / A  
detached - отсоединенная (true) или присоединенная (false) подпись  

В ответ вернется json с cms в base64 и параметрами подписи

# Доступен Swagger
```http://localhost:8080/swagger-ui/index.html```