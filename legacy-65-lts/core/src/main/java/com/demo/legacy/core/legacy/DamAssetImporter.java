package com.demo.legacy.core.legacy;

import java.io.InputStream;

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;

/**
 * LEGACY: streams binaries into the repository with AssetManager.createAsset. On Cloud Service,
 * uploads go through direct binary upload and asset processing runs in Asset compute microservices.
 */
@Component(service = DamAssetImporter.class)
public class DamAssetImporter {

    public Asset importAsset(ResourceResolver resolver, String path, InputStream binary, String mimeType) {
        AssetManager assetManager = resolver.adaptTo(AssetManager.class);
        if (assetManager == null) {
            return null;
        }
        return assetManager.createAsset(path, binary, mimeType, true);
    }
}
