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
    private Long mediaFileId;

    @NotNull(message = "El título asociado es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_title_id", nullable = false)
    private MediaTitle mediaTitle;

    @NotNull(message = "El tipo de archivo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    @NotNull(message = "La URL del blob es obligatoria")
    @Column(name = "blob_url", length = 500, nullable = false)
    private String blobUrl;

    @Column(name = "etag", length = 100)
    private String etag;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "uploaded_by", length = 50)
    private String uploadedBy;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }

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

        public String getDisplayName() { return displayName; }
        public String[] getAllowedContentTypes() { return allowedContentTypes; }
        public Long getMaxSizeBytes() { return maxSizeBytes; }

        public boolean isValidContentType(String contentType) {
            if (contentType == null) return false;
            for (String allowed : allowedContentTypes) {
                if (contentType.equalsIgnoreCase(allowed)) return true;
            }
            return false;
        }

        public boolean isValidSize(Long sizeBytes) {
            return sizeBytes != null && sizeBytes <= maxSizeBytes;
        }
    }

    // Getters y Setters
    public Long getMediaFileId() { return mediaFileId; }
    public void setMediaFileId(Long mediaFileId) { this.mediaFileId = mediaFileId; }

    public MediaTitle getMediaTitle() { return mediaTitle; }
    public void setMediaTitle(MediaTitle mediaTitle) { this.mediaTitle = mediaTitle; }

    public FileType getFileType() { return fileType; }
    public void setFileType(FileType fileType) { this.fileType = fileType; }

    public String getBlobUrl() { return blobUrl; }
    public void setBlobUrl(String blobUrl) { this.blobUrl = blobUrl; }

    public String getEtag() { return etag; }
    public void setEtag(String etag) { this.etag = etag; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}