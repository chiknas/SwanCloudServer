# Swan Cloud Server

Spring boot api to handle photo/video files upload and organisation. The server will save uploaded media files in the file system that is running on and in the specified base path (see env variables below). It organises the media files based on the date the photo/video was taken.

## Env variables

1. files.base-path = the base path the system will use to start organising the uploaded files. Use when you need to mount a volume when using this with Docker. Default setting is the base path the application was initialized on.

## Useful commands

1. Build docker file
   `docker build -t swancloudserver:latest .`

2. Run docker image
   `sudo docker run -p 8080:8080 swancloudserver`

3. Upload image with curl
   `curl -F 'file=@/image/path/here/image.png' -o - http://localhost:8080/api/upload`
