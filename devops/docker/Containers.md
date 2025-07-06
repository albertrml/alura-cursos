# Contêineres
O contêiner é uma solução de virtualização que consiste em um ambiente isolado, alocado em um servidor, que compartilha um único host de controle.
#Diferenças entre Máquinas Virtuais e Contêineres

Em uma máquina virtual (VM), o funcionamento se dá com o hardware físico, seguido pelo sistema operacional instalado. Acima dessa camada, encontra-se o hypervisor, responsável por isolar o funcionamento do software de virtualização do sistema operacional hospedeiro. Dentro desse ambiente, podemos ter diferentes sistemas operacionais, independentes do sistema hospedeiro, e prepará-los com as dependências necessárias, configurando partes do software conforme necessário. Esta é a abordagem típica de virtualização usando máquinas virtuais.

| **Frontend**        | **Backend**         | **Bando de Dados**  |
| ------------------- | ------------------- | ------------------- |
| Dependências        | Dependências        | Dependências        |
| SO 1 Virtualizado   | SO 2 Virtualizado   | SO 3 Virtualizado   |
| Hypervisor          | Hypervisor          | Hypervisor          |
| Sistema Operacional | Sistema Operacional | Sistema Operacional |
| Hardware            | Hardware            | Hardware            |

Por outro lado, os contêineres funcionam de maneira um pouco diferente. Embora também tenhamos as camadas de hardware e sistema operacional, não há uma camada de hypervisor. Em vez disso, os contêineres são executados diretamente sobre o sistema operacional hospedeiro, eliminando a necessidade dessa camada de isolamento. Isso é possível graças aos namespaces, que garantem diferentes níveis de isolamento entre os contêineres.

| **Frontend**        | **Backend**         | **Bando de Dados**  |
| ------------------- | ------------------- | ------------------- |
| Dependências        | Dependências        | Dependências        |
| Container 1         | Container 2         | Container 3         |
| Sistema Operacional | Sistema Operacional | Sistema Operacional |
| Hardware            | Hardware            | Hardware            |

Nos contêineres, não é necessário ter um sistema operacional completo instalado. Podemos utilizar apenas as partes necessárias do sistema e acessar o kernel do sistema operacional hospedeiro, aumentando a eficiência e facilitando o uso de recursos. Enquanto nas VMs era preciso alocar recursos específicos para cada VM, nos contêineres, eles são executados como processos isolados dentro da máquina, proporcionando uma gestão eficiente dos recursos por meio dos cgroups.

Além disso, os contêineres utilizam diversos namespaces para o isolamento em diferentes níveis:

PID: provê isolamento dos processos rodando dentro do contêiner;
NET: provê isolamento das interfaces de rede;
IPC: provê isolamento da comunicação entre processos e memória compartilhada;
MNT: provê isolamento do sistema de arquivos/pontos de montagem;
UTS: provê isolamento do kernel. Age como se o contêiner fosse outro host.
