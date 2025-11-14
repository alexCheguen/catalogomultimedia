package com.catalogomultimedia.service;


import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.sas.SasProtocol;
import com.catalogomultimedia.dtos.MediaFileDTO;
import com.catalogomultimedia.enums.FileType;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Supplier;

@ApplicationScoped
public class AzureBlobStorageService {
    private BlobServiceClient blobServiceClient;
    private BlobContainerClient container;
    private static final String CONTAINER_NAME = "catalogos";

    public AzureBlobStorageService() {

    }

    @PostConstruct
    void init() {
        String conn = System.getProperty("AZURE_STORAGE_CONNECTION_STRING");
        if (conn == null || conn.isBlank()) {
            throw new IllegalStateException("AZURE_STORAGE_CONNECTION_STRING no está definida.");
        }

        blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(conn).buildClient();

        container = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
        if (!container.exists()) container.create();
    }

    /**
     * Crea el contenedor si no existe
     */
    private BlobContainerClient ensureContainer(String name) {
        BlobContainerClient client = blobServiceClient.getBlobContainerClient(name);
        if (!client.exists()) client.create();
        return client;
    }

    /**
     * Sube un archivo al contenedor según la estructura:
     * posters/{title_name}/{timestamp}.jpg o fichas/{title_name}/{timestamp}.pdf
     */
    public MediaFileDTO uploadCatalogFile(FileType fileType,
                                          String titleName,
                                          String originalFileName,
                                          String contentType,
                                          InputStream data,
                                          long sizeBytes,
                                          String uploadedBy,
                                          Duration sasTtl,
                                          boolean openInline) {


        String safeTitle = slug(titleName);
        String ext = guessExtension(originalFileName, contentType, fileType);

        // Fallback de content-type si viene vacío
        if (contentType == null || contentType.isBlank() || "application/octet-stream".equalsIgnoreCase(contentType)) {
            contentType = switch (ext.toLowerCase(Locale.ROOT)) {
                case "png" -> "image/png";
                case "jpg", "jpeg" -> "image/jpeg";
                case "pdf" -> "application/pdf";
                default -> "application/octet-stream";
            };
        }

        String blobPath = buildPath(fileType, safeTitle, ext);
        BlockBlobClient blob = container.getBlobClient(blobPath).getBlockBlobClient();

        BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("uploadedBy", uploadedBy != null ? uploadedBy : "unknown");
        metadata.put("fileType", fileType.name());
        metadata.put("titleName", safeTitle);


        blob.uploadWithResponse(
                new BlockBlobSimpleUploadOptions(BinaryData.fromStream(data, sizeBytes))
                        .setHeaders(headers)
                        .setMetadata(metadata),
                null, null
        );

        BlobProperties props = blob.getProperties();


        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(365);

        BlobSasPermission sasPermission = new BlobSasPermission()
                .setReadPermission(true);

        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expiryTime, sasPermission)
                .setStartTime(OffsetDateTime.now().minusMinutes(5));

        String BlobSasUrl = this.buildBlobSasUrl(blob, Duration.ofDays(365), true, blob.getBlobName(), contentType);

        // DTO
        MediaFileDTO dto = new MediaFileDTO();
        dto.setBlobName(blobPath);
        dto.setPublicUrl(blob.getBlobUrl());
        dto.setSignedUrl(BlobSasUrl);
        dto.setEtag(props.getETag());
        dto.setContentType(props.getContentType());
        dto.setSizeBytes(props.getBlobSize());
        dto.setFileType(fileType);
        dto.setUploadedAt(OffsetDateTime.now(ZoneOffset.UTC));
        dto.setUploadedBy(uploadedBy);

        return dto;
    }

    /**
     * Obtiene un blob por su nombre
     */
    public Optional<MediaFileDTO> getBlob(String blobName) {
        BlockBlobClient blob = container.getBlobClient(blobName).getBlockBlobClient();
        if (!blob.exists()) return Optional.empty();

        BlobProperties props = blob.getProperties();
        MediaFileDTO dto = new MediaFileDTO();
        dto.setBlobUrl(blob.getBlobUrl());
        dto.setBlobName(blobName);
        dto.setEtag(props.getETag());
        dto.setContentType(props.getContentType());
        dto.setSizeBytes(props.getBlobSize());
        dto.setUploadedAt(props.getLastModified());
        dto.setUploadedBy(getMeta(props.getMetadata(), "uploadedBy"));
        dto.setFileType(fileTypeFromMetaOr(inferTypeByPath(blobName), props.getMetadata()));

        return Optional.of(dto);
    }

    /**
     * Lista todos los blobs o por prefijo
     */
    public List<MediaFileDTO> listBlobs(String prefix) {
        List<MediaFileDTO> result = new ArrayList<>();
        PagedIterable<BlobItem> items = (prefix == null || prefix.isEmpty())
                ? container.listBlobs()
                : container.listBlobsByHierarchy("/");

        for (BlobItem item : items) {
            String name = item.getName();
            BlockBlobClient blob = container.getBlobClient(name).getBlockBlobClient();
            BlobProperties props = safe(() -> blob.getProperties(), null);
            if (props == null) continue;

            MediaFileDTO dto = new MediaFileDTO();
            dto.setBlobUrl(blob.getBlobUrl());
            dto.setBlobName(name);
            dto.setEtag(props.getETag());
            dto.setContentType(props.getContentType());
            dto.setSizeBytes(props.getBlobSize());
            dto.setUploadedAt(props.getLastModified());
            dto.setUploadedBy(getMeta(props.getMetadata(), "uploadedBy"));
            dto.setFileType(fileTypeFromMetaOr(inferTypeByPath(name), props.getMetadata()));
            result.add(dto);
        }
        return result;
    }

    public List<MediaFileDTO> listAllBlobs() {
        return listBlobs(null);
    }

    /**
     * Elimina físicamente un blob
     */
    public boolean deleteBlob(String blobName) {
        BlockBlobClient blob = container.getBlobClient(blobName).getBlockBlobClient();
        if (!blob.exists()) return false;
        blob.delete();
        return true;
    }

    /**
     * Genera una URL SAS de lectura temporal
     */
    public String generateBlobReadSasUrl(String blobName, Duration ttl) {
        BlobClient blobClient = container.getBlobClient(blobName);
        return this.buildBlobSasUrl(blobClient.getBlockBlobClient(), ttl, true, blobClient.getBlobName(), null);
    }

    /* -------------------- Helpers -------------------- */

    private static <T> T safe(Supplier<T> sup, T def) {
        try {
            return sup.get();
        } catch (Exception e) {
            return def;
        }
    }

    private static String slug(String text) {
        if (text == null) return "untitled";
        return text.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private static String guessExtension(String fileName, String contentType, FileType type) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }
        if ("application/pdf".equalsIgnoreCase(contentType)) return "pdf";
        if ("image/png".equalsIgnoreCase(contentType)) return "png";
        if ("image/jpeg".equalsIgnoreCase(contentType)) return "jpg";
        return (type == FileType.TECHNICAL_SHEET) ? "pdf" : "jpg";
    }

    private static String buildPath(FileType type, String safeTitle, String ext) {
        String folder = (type == FileType.POSTER) ? "posters" : "fichas";
        long ts = System.currentTimeMillis();
        return String.format("%s/%s/%d.%s", folder, safeTitle, ts, ext);
    }

    private static String getMeta(Map<String, String> meta, String key) {
        return (meta != null && meta.containsKey(key)) ? meta.get(key) : null;
    }

    private static FileType fileTypeFromMetaOr(FileType fallback, Map<String, String> meta) {
        if (meta != null && meta.containsKey("fileType")) {
            try {
                return FileType.valueOf(meta.get("fileType"));
            } catch (Exception ignored) {
            }
        }
        return fallback;
    }

    private static FileType inferTypeByPath(String blobName) {
        if (blobName.startsWith("posters/")) return FileType.POSTER;
        if (blobName.startsWith("fichas/")) return FileType.TECHNICAL_SHEET;
        return null;
    }

    private String buildBlobSasUrl(BlockBlobClient blob,
                                   Duration ttl,
                                   boolean openInline,
                                   String downloadFileName,
                                   String contentType) {

        OffsetDateTime starts = OffsetDateTime.now().minusMinutes(5);
        OffsetDateTime expires = OffsetDateTime.now().plus(ttl);

        BlobSasPermission perm = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues sv = new BlobServiceSasSignatureValues(expires, perm)
                .setStartTime(starts)
                .setProtocol(SasProtocol.HTTPS_ONLY);

        if (contentType != null && !contentType.isBlank()) {
            sv.setContentType(contentType);
        }

        if (downloadFileName != null && !downloadFileName.isBlank()) {
            String disp = openInline
                    ? "inline; filename=\"" + downloadFileName + "\""
                    : "attachment; filename=\"" + downloadFileName + "\"";
            sv.setContentDisposition(disp);
        }

        String sasToken = blob.generateSas(sv);
        return blob.getBlobUrl() + "?" + sasToken;
    }
}
