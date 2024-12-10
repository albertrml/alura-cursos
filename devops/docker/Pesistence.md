# *Bind Mount*
Todos os dados que forem criados serão armazenados dentro do diretório, por exemplo, app do container, persistindo, assim, no próprio file system da máquina.

```bash
$ mkdir volume-docker

$ docker run -it --mount type=bind, source=/home/alura/volume-docker, target=/app ubuntu bash
```

Portaanto, o problema do mecanismo do *Bind Mount* consiste na falta gerência pelo docker, isto é, o arquivo é alcançável for do docker, podendo ser danificado.

# Volume

Dentro do File System (Sistema de Arquivos), existe uma área do Docker, que é um enclave um pouco mais seguro dentro do nosso host. Essa área está interligada diretamente ao container pelo mecanismo dos volumes. 

Sendo mais reservada, a área de volume é menos vunerável aos usuários do sistema. Por isso, para ambientes de produção, o Docker recomenda oficialmente que seja utilizado o mecanismo dos volumes na persistência de dados.

```bash
$ docker volume create novo-volume

docker run -it --mount source=novo-volume,target=/app ubuntu bash
```

# TMFS
A ideia do tmpfs é armazenar os dados de forma temporária, para proteger esses dados de acessos externos. 
```bash
$ docker run -it --tmpfs=/app ubuntu bash
```