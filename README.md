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
* указать считыватель, алиас (имя контейнера) и пин код в application.yml
* установить корневой и промежуточный сертификаты в cacerts

# Сборка
```shell
./gradlew bootJar
```

# Запуск
```shell
./gradlew bootRun
```

# Использование
## подпись
```shell
curl http://localhost:8080/sign -F data=@DATA [ -F tsp=http://TSP_SERVER/tsp/tsp.srf -F type=TYPE -F detached=true -F encodeToB64=true ]
```
где  
DATA - файл который необходимо подписать  
TSP_SERVER - адрес сервера службы TSP  
TYPE - тип подписи. допустимые значения: BES / T / XLT1 / A  
detached - отсоединенная (true) или присоединенная (false) подпись  
encodeToB64 - в какой кодировке вернуть подпись. Base64 (true) или DER (false) 

## проверка подписи
```shell
curl http://localhost:8080/verify -F sign=@SIGN [ -F data=@DATA ]
```
где  
SIGN - файл подписи
DATA - исходный файл для проверки в случае detached подписи  

## зашифрование
```shell
curl http://localhost:8080/encr -F data=@DATA [ -F cert=@CERT1 -F cert=@CERT2 -F encodeToB64=true ]
```
где  
DATA - файл который необходимо зашифровать  
CERTN - сертификат(ы) получателя(ей),   
encodeToB64 - в какой кодировке вернуть подпись. Base64 (true) или DER (false)

## расшифрование
```shell
curl http://localhost:8080/decr -F cms=@CMS [ -F cert=@CERT1 -F cert=@CERT2 -F encodeToB64=true ]
```
где  
CMS - файл который необходимо расшифровать  


# Доступен Swagger
```http://localhost:8080/swagger-ui/index.html```