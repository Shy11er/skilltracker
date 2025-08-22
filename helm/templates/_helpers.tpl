{{- define "app.name" -}}{{ .Chart.Name }}{{- end }}

{{- define "app.labels" -}}
app.kubernetes.io/name: {{ include "app.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{-/* Bitnami PostgreSQL service name: <fullname>-primary.
      При fullnameOverride: postgresql ⇒ postgresql-primary */ -}}
{{- define "app.pg.host" -}}
{{ printf "%s-primary" .Values.postgresql.fullnameOverride | quote }}
{{- end }}
