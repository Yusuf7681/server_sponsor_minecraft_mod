package com.yusufemin.sponsor;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SponsorModClient implements ClientModInitializer {
    public static final String MOD_ID = "server_sponsor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        SponsorConfig.load();
        LOGGER.info("Server Sponsor Button loaded.");
    }
}
