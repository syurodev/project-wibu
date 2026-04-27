package com.syuro.wibusystem.work.novel.dto;

import jakarta.validation.constraints.NotNull;

public class CreateNovelVolumeRequest {
    @NotNull
    private long workId;

    int volumeNumber;

    String cover;


}
