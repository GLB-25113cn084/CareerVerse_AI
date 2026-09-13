package com.careerverse.util;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
public class TokenUtil {
  public static String randomToken(){ byte[] b=new byte[32]; new SecureRandom().nextBytes(b); return HexFormat.of().formatHex(b); }
  public static String sha256(String s){ try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes())); } catch(Exception e){ throw new RuntimeException(e); } }
}
