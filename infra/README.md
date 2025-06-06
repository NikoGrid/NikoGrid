## NikoGrid Infrastructure

This directory contains the files needed to provision a server capable of
running nikogrid, and the application itself.

### Provision the server

The server needs to be either ubuntu or debian based, both ubuntu 22.04 and
debian 12 (bookworm) were tested. In case of debian the `python3-xyz` package
must be installed (`python3-11` was tested).

```sh
# apt install python3-11
```

Afterwards the `inventory.yml` should be updated to contain the server address.
Finally the ansible playbook (`playbook.yml`) can be deployed.

```sh
$ ansible-playbook -i inventory.yml playbook.yml
```

> [!NOTE]  
> Access to the server must be configured with public keys because the playbook
> process needs unattended access to the server.

### Get the kubeconfig for access

```sh
$ scp root@<server>:/etc/rancher/k3s/k3s.yaml ~/.kube/config
```

### Get the secret to access ArgoCD

```sh
$ argocd admin initial-password -n argocd
```

### Set the github bot user account password

```sh
argocd account update-password \
    --account github \
    --current-password <admin-password> \
    --new-password <github-user-password>
```

### Adding the monitoring stack:

```sh
$ kubectl create -n monitoring secret generic monitoring-stack-grafana \
    --from-literal=admin-user='admin' \
    --from-literal=admin-password='<grafana-password>'
$ kubectl apply -n argocd -f monitoring/monitoring-stack-app.yml
$ kubectl apply -n argocd -f monitoring/loki-app.yml
$ kubectl apply -n argocd -f monitoring/grafana-alloy-app.yml
```

### Adding the self hosted github runners

Create a github acess token at https://github.com/settings/tokens/new?scopes=repo
this will be used for the runner. Then create the resources:

```sh
$ kubectl create namespace arc-runners
$ kubectl create -n arc-runners secret generic arc-github-secret \
      --from-literal=github_token='<github-secret>'
$ kubectl apply -n argocd -f github-arc
```

### Creating the staging application

Create a github acess token at https://github.com/settings/tokens/new?scopes=repo
this will be used for the runner. Then create the resources:

```sh
$ kubectl apply -n argocd -f argocd/nikogrid-project.yml -f staging-app.yml
```
