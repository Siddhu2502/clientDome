package com.kmkm.clientdome.dto;

import java.io.Serializable;

// for fixing the serilization error
public record ChatMessage(String role, String text) implements Serializable {}