package tech.nitidez.valarlibrary.lib.profile;

public class InvalidMojangException extends Exception {
  
  private static final long serialVersionUID = 1L;
  
  public InvalidMojangException(String msg) {
    super(msg);
  }
}
