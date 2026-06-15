# MenuAdmin — Kubernetes deployment

Manifests that deploy the **menus-admin** web app (Kotlin/Compose Multiplatform → WASM,
served by Nginx) to the AppToLast Kubernetes cluster.

## Target environment

| Item              | Value                                                                 |
|-------------------|-----------------------------------------------------------------------|
| Cluster           | `apptolastserver` (kubeadm, single node), node IP `138.199.157.58`    |
| Namespace         | `apptolast-menus-admin-dev`                                           |
| Public URL        | https://menusadmin.apptolast.com                                      |
| Image             | `apptolast/menus-admin:latest` (public on Docker Hub)                |
| Ingress           | Traefik (`websecure` entrypoint)                                      |
| TLS               | cert-manager `ClusterIssuer/cloudflare-clusterissuer` (Let's Encrypt, Cloudflare DNS-01) → secret `menusadmin-tls` |
| DNS               | Cloudflare A record `menusadmin.apptolast.com → 138.199.157.58` (DNS only, not proxied) |

This mirrors the sibling admin frontends already running in the cluster
(`apptolast-inemsellar-admin-dev` / `inemadmin.apptolast.com`,
`apptolast-greenhouse-admin-dev` / `greenhouseadmin.apptolast.com`).

## Files

| File                      | Resource                                                        |
|---------------------------|-----------------------------------------------------------------|
| `00-namespace.yaml`       | Namespace `apptolast-menus-admin-dev`                          |
| `10-deployment.yaml`      | Deployment `menus-admin` (2 replicas, `:latest`, `/health` probes) |
| `20-service.yaml`         | Service `menus-admin` (ClusterIP :80)                          |
| `30-ingress.yaml`         | Ingress `menus-admin` (Traefik + cert-manager TLS)            |
| `40-rollout-rbac.yaml`    | ServiceAccount + Role + RoleBinding for the auto-rollout job   |
| `50-rollout-cronjob.yaml` | CronJob `menus-admin-rollout` — every 5 h re-pulls `:latest`   |

## Prerequisites (one-time)

1. **DNS** — a Cloudflare A record `menusadmin.apptolast.com → 138.199.157.58`
   (DNS only, not proxied). This record already exists in the `apptolast.com` zone.
   Note: TLS issuance does **not** depend on this A record — the
   `cloudflare-clusterissuer` uses an ACME **DNS-01** challenge (a temporary `TXT`
   record), so cert-manager can obtain the certificate independently. The A record
   only governs end-user reachability of the site.
2. The image `apptolast/menus-admin:latest` must be published (handled by the
   `.github/workflows/ci-cd.yml` pipeline on `main` / manual `workflow_dispatch`).

## Apply

```bash
kubectl apply -f k8s/
```

## Verify

```bash
kubectl get pods,svc,ingress -n apptolast-menus-admin-dev
kubectl get certificate -n apptolast-menus-admin-dev          # menusadmin-tls → READY=True
curl -fsS https://menusadmin.apptolast.com/health             # → OK
```

## Updating the deployment

New images pushed to `apptolast/menus-admin:latest` are picked up automatically
by the `menus-admin-rollout` CronJob (every 5 h). To roll out immediately:

```bash
kubectl rollout restart deployment/menus-admin -n apptolast-menus-admin-dev
```
