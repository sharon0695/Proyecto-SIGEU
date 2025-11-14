package com.gestion.eventos.DTO;

    public class EvaluacionRequest {
        private Integer codigoEvento;
        private Integer idSecretaria;
        private String observaciones;
        private org.springframework.web.multipart.MultipartFile actaComite;

        public Integer getCodigoEvento() { return codigoEvento; }
        public void setCodigoEvento(Integer codigoEvento) { this.codigoEvento = codigoEvento; }
        
        public Integer getIdSecretaria() { return idSecretaria; }
        public void setIdSecretaria(Integer idSecretaria) { this.idSecretaria = idSecretaria; }
        
        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
        
        public org.springframework.web.multipart.MultipartFile getActaComite() { return actaComite; }
        public void setActaComite(org.springframework.web.multipart.MultipartFile actaComite) { this.actaComite = actaComite; }
    }