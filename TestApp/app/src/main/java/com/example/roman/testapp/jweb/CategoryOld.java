/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jweb;

import java.net.URL;



/**
 *
 * @author Roman Zelenik
 */
public class CategoryOld {
  private final int id;
  private final String name;
  private final URL image;

  public CategoryOld(int id, String name, URL image) {
    this.id = id;
    this.name = name;
    this.image = image;
  }

  @Override
  public String toString() {
    return name;
  }
  
  
}
