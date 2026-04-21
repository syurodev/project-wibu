package com.syuro.wibusystem.creator.controller.v1;

import com.syuro.wibusystem.creator.service.CreatorProfileService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/creator-profiles")
public class CreatorProfileController {
    final CreatorProfileService creatorProfileService;

    public CreatorProfileController(
            CreatorProfileService creatorProfileService
    ) {
        this.creatorProfileService = creatorProfileService;
    }
}
