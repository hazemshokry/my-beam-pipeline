variable "job_name" {
  type = string
}

variable "temp_gcs_location" {
  type = string
}

variable "template_gcs_path" {
  type = string
}

resource "google_dataflow_job" "big_data_job" {
  name              = var.job_name
  temp_gcs_location = var.temp_gcs_location
  template_gcs_path = var.template_gcs_path
}