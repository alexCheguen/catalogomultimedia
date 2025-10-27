package com.catalogomultimedia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "media_files")
public class MediaFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_file_id")
    private Long id;

    @NotNull(message = "El título asociado es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_title_id", nullable = false)
    private MediaTitle mediaTitle;

    @NotNull(message = "El tipo de archivo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 20)
    private FileType fileType;

    @NotBlank(message = "La URL del blob es obligatoria")
    @Size(max = 500, message = "La URL no puede exceder 500 caracteres")
    @Column(name = "blob_url", nullable = false, length = 500)
    private String blobUrl;

    @Size(max = 100, message = "El ETag no puede exceder 100 caracteres")
    @Column(name = "etag", length = 100)
    private String etag;

    @Size(max = 50, message = "El tipo de contenido no puede exceder 50 caracteres")
    @Column(name = "content_type", length = 50)
    private String contentType;

    @Positive(message = "El tamaño debe ser un valor positivo")
    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Size(max = 50, message = "El nombre de usuario no puede exceder 50 caracteres")
    @Column(name = "uploaded_by", length = 50)
    private String uploadedBy;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // 🔹 Enumeración para tipos de archivo
    public enum FileType {
        POSTER("Póster", new String[]{"image/jpeg", "image/png"}, 2 * 1024 * 1024L),
        TECHNICAL_SHEET("Ficha Técnica", new String[]{"application/pdf"}, 5 * 1024 * 1024L);

        private final String displayName;
        private final String[] allowedContentTypes;
        private final Long maxSizeBytes;

        FileType(String displayName, String[] allowedContentTypes, Long maxSizeBytes) {
            this.displayName = displayName;
            this.allowedContentTypes = allowedContentTypes;
            this.maxSizeBytes = maxSizeBytes;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String[] getAllowedContentTypes() {
            return allowedContentTypes;
        }

        public Long getMaxSizeBytes() {
            return maxSizeBytes;
        }

        public boolean isValidContentType(String contentType) {
            if (contentType == null) return false;
            for (String allowed : allowedContentTypes) {
                if (contentType.equalsIgnoreCase(allowed)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isValidSize(Long sizeBytes) {
            return sizeBytes != null && sizeBytes > 0 && sizeBytes <= maxSizeBytes;
        }

        public String getMaxSizeMB() {
            return String.format("%.0f MB", maxSizeBytes / (1024.0 * 1024.0));
        }
    }

    // 🔹 Constructores
    public MediaFile() {
    }

    public MediaFile(MediaTitle mediaTitle, FileType fileType, String blobUrl) {
        this.mediaTitle = mediaTitle;
        this.fileType = fileType;
        this.blobUrl = blobUrl;
    }

    // 🔹 Lifecycle callback
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    // 🔹 Getters y Setters
    public Long getMediaFileId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MediaTitle getMediaTitle() {
        return mediaTitle;
    }

    public void setMediaTitle(MediaTitle mediaTitle) {
        this.mediaTitle = mediaTitle;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public String getBlobUrl() {
        return blobUrl;
    }

    public void setBlobUrl(String blobUrl) {
        this.blobUrl = blobUrl;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // 🔹 Métodos utilitarios
    public String getSizeFormatted() {
        if (sizeBytes == null) return "N/A";

        if (sizeBytes < 1024) {
            return sizeBytes + " bytes";
        } else if (sizeBytes < 1024 * 1024) {
            return String.format("%.2f KB", sizeBytes / 1024.0);
        } else {
            return String.format("%.2f MB", sizeBytes / (1024.0 * 1024.0));
        }
    }

    public boolean isPoster() {
        return FileType.POSTER.equals(fileType);
    }

    public boolean isTechnicalSheet() {
        return FileType.TECHNICAL_SHEET.equals(fileType);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MediaFile{");
        sb.append("id=").append(id);
        sb.append(", fileType=").append(fileType);
        sb.append(", contentType='").append(contentType).append('\'');
        sb.append(", sizeBytes=").append(sizeBytes);
        sb.append(", uploadedAt=").append(uploadedAt);
        sb.append(", uploadedBy='").append(uploadedBy).append('\'');
        sb.append(", isActive=").append(isActive);
        sb.append('}');
        return sb.toString();
    }
}