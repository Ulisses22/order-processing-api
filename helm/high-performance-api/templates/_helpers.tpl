{{- define "high-performance-api.name" -}}
{{- .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "high-performance-api.fullname" -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "high-performance-api.labels" -}}
app: {{ include "high-performance-api.name" . }}
{{- end }}
