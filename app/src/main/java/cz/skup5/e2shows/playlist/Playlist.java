package cz.skup5.e2shows.playlist;

/**
 * @author Skup5
 */
public interface Playlist<Item extends PlaylistItem> {

  /**
   * Returns the last selected {@link PlaylistItem} or null if none item wasn't selected yet.
   *
   * @return playlist item or null
   */
  Item actual();

  /**
   * Returns {@link PlaylistItem} after {@link #actual()}.
   * If {@code actual} is null, it is called {@link #first()}.
   * If {@code actual} is {@code last}, returns null;
   *
   * @return playlist item after {@code actual}
   */
  Item next();

  /**
   * Returns {@link PlaylistItem} before {@link #actual()}.
   * If {@code actual} is null or {@code first}, returns null.
   *
   * @return playlist item before {@code actual}
   */
  Item previous();

  /**
   * @return
   */
  Item first();

  /**
   * @return
   */
  Item last();

  /**
   * Index of item in playlist.
   *
   * @param item the requested item
   * @return index of item or -1 if doesn't contain this {@code item}
   */
  int indexOf(Item item);
}
