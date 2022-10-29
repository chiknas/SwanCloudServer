# Swan Cloud Server ![CircleCI Build Status](https://img.shields.io/circleci/build/github/chiknas/SwanCloudServer)

Spring boot api to handle photo/video files upload and organisation. The server will save uploaded media files in the
file system that is running on and in the specified base path (see env variables below). It organises the media files
based on the date the photo/video was taken.  
Run the server and visit [Swagger 3](http://localhost:8080/swagger-ui/index.html) to get details on the API.

## QuickStart

1. Build the project: `./gradlew build`
2. Update `application.properties` with your settings
3. Startup the server: `./gradlew bootRun`
4. Application api is up at: `http://locahost:8080/api`

## Spring Profiles

1. production = profile to be switch the app to production mode. Current functionality on production mode:
    * API keys enabled: the server will only respond to known clients. set this up with the security.api.keys property
    * HTTPS only (check security section to setup certificate vars in this mode)

## Env variables

### <ins>General</ins>

1. files.base-path = the base path the system will use to start organising the uploaded files. Use when you need to
   mount a volume when using this with Docker. Default setting is the base path the application was initialized on.

### <ins>Security('production' only)</ins>

1. GMAIL_ADMIN_ACCOUNTS = Comma separated list of gmail accounts that have access to this system. Everyone else is
   kicked out.
   Security is handled with OAuth2 and Google.

2. OAUTH2_CLIENT_ID = Client ID taken from https://console.cloud.google.com/apis/credentials you have setup for your
   app.

3. OAUTH2_CLIENT_SECRET = Client Secret taken from https://console.cloud.google.com/apis/credentials you have setup for
   your app.

4. server.ssl.key-store = the path where the keystore is located. only PKCS12 key-stores are allowed.

5. server.ssl.key-alias = the alias of the key to look for in your certificate

6. server.ssl.key-store-password = the password used to generate the keystore

## Useful commands

1. Build docker file
   `docker build -t swancloudserver:latest .`

2. Run docker image
   `sudo docker run -p 8080:8080 swancloudserver`

3. Upload image with curl
   `curl -F data=@/image/path/here/image.png -o - http://localhost:8080/api/upload`

4. Generate PKCS12 key-store with jvm keytool
   `keytool -genkeypair -alias swancloud -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore swancloud.p12 -validity 3650`
