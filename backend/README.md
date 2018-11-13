# Jugz backend

### Running
`docker run -it --net=host scf37/jugz-backend`

To get help, try

`docker run -it --net=host scf37/jugz-backend --help`


### Building

```
sbt pack
docker build -t scf37/jugz-backend .
```
