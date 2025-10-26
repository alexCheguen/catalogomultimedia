package com.catalogomultimedia.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.catalogomultimedia.entity.MediaFile.FileType;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Singleton
@Startup
public class AzureBlobStorageService {

    private static final String CONNECTION_STRING = System.getProperty(
            "azure.storage.connection-string",
            "DefaultEndpointsProtocol=https;AccountName=catalogospg;AccountKey=YOUR_KEY;EndpointSuffix=core.windows.net"
    );

    private static final String CONTAINER_NAME = System.getProperty(
            "azure.storage.container-name", "catalogos"
    );

    private BlobServiceClient blobServiceClient;
    private BlobContainerClient containerClient;

    @PostConstruct
    public void init() {
        blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(CONNECTION_STRING)
                .buildClient();

        containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);

        if (!containerClient.exists()) {
            containerClient.create();
        }
    }

    public BlobUploadResult uploadFile(InputStream inputStream, long size,
                                       String contentType, String titleName,
                                       FileType fileType, String originalFilename)
            throws IOException {

        // Validar tipo de archivo
        if (!fileType.isValidContentType(contentType)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no válido. Tipos permitidos: " +
                            String.join(", ", fileType.getAllowedContentTypes())
            );
        }

        // Validar tamaño
        if (!fileType.isValidSize(size)) {
            throw new IllegalArgumentException(
                    "El archivo excede el tamaño máximo permitido de " +
                            (fileType.getMaxSizeBytes() / 1024 / 1024) + " MB"
            );
        }

        String blobName = generateBlobName(titleName, fileType, originalFilename);
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);

        blobClient.upload(inputStream, size, true);
        blobClient.setHttpHeaders(headers);

        BlobProperties properties = blobClient.getProperties();

        BlobUploadResult result = new BlobUploadResult();
        result.setBlobUrl(blobClient.getBlobUrl());
        result.setEtag(properties.getETag());
        result.setContentType(contentType);
        result.setSizeBytes(size);
        result.setBlobName(blobName);

        return result;
    }

    public String generateSasUrl(String blobName, int expirationMinutes) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plusMinutes(expirationMinutes),
                permission
        );

        String sasToken = blobClient.generateSas(values);
        return blobClient.getBlobUrl() + "?" + sasToken;
    }

    public boolean deleteBlob(String blobName) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            return blobClient.deleteIfExists();
        } catch (Exception e) {
            return false;
        }
    }

    public String getContainerName() {
        return CONTAINER_NAME;
    }

    private String generateBlobName(String titleName, FileType fileType, String originalFilename) {
        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String sanitizedTitle = titleName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        String extension = getFileExtension(originalFilename);

        String folder = fileType == FileType.POSTER ? "posters" : "fichas";

        return String.format("%s/%s/%s%s", folder, sanitizedTitle, timestamp, extension);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public static class BlobUploadResult {
        private String blobUrl;
        private String etag;
        private String contentType;
        private Long sizeBytes;
        private String blobName;

        // Getters y Setters
        public String getBlobUrl() { return blobUrl; }
        public void setBlobUrl(String blobUrl) { this.blobUrl = blobUrl; }

        public String getEtag() { return etag; }
        public void setEtag(String etag) { this.etag = etag; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public Long getSizeBytes() { return sizeBytes; }
        public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }

        public String getBlobName() { return blobName; }
        public void setBlobName(String blobName) { this.blobName = blobName; }
    }
}