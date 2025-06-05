import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/*
 * 1) Work through the test cases in this file, top to bottom, making them pass one by one.
 *
 * 2) Try to do the simplest thing possible to make the test pass, even if it is silly or ugly.
 *
 * 2) You can add new tests to validate any ideas, but do not change the existing tests.
 *
 * 4) Once a test passes, consider if there is anything you can do to make the code better.
 */
class SplitterTest {


    /*
     * Let's get started with something trivial...
     *
     * When given an empty message the spiltIntoParts method should return a list containing one item that is the empty
     * string
     */
    @Test
    @Order(1)
    void warming_up() {
        assertEquals(List.of(""), Splitter.splitIntoParts("", Splitter.Encoding.GSM));
    }

    /*
     * An SMS Message can contain up to 140 bytes of message content!. The common default character encoding
     * is the GSM character encoding. The GSM character encoding uses 7 bits per character. This means that it is
     * possible to hold 160 GSM characters in a single message part.
     *
     * Start by making the splitIntoParts method handle a single GSM message using every last bit!
     */
    @Test
    @Order(2)
    void gsm_single_part_message() {
        var message160Chars = "0123456789".repeat(16);

        var splitMessage = Splitter.splitIntoParts(message160Chars, Splitter.Encoding.GSM);

        assertEquals(List.of(message160Chars), splitMessage);
    }

    /*
     * GSM is quite limiting as a character set and does not support fun things like emoji.
     *
     * The default Unicode encoding for SMS is UCS-2 with its modern version UTF-16 widely supported
     *
     * This means that when encoding a message as Unicode using UTF-16 each character will take at least two bytes.
     * Two byes per character restricts Unicode SMS to 70 characters in a single part
     *
     * Enhance the splitIntoParts method support a single Unicode SMS.
     */
    @Test
    @Order(3)
    void unicode_single_part_message() {
        var message70Chars = "0123456789".repeat(7);

        var splitMessage = Splitter.splitIntoParts(message70Chars, Splitter.Encoding.Unicode);

        assertEquals(List.of(message70Chars), splitMessage);
    }

    /*
     * 160 characters for GSM (or even just 70 for Unicode) is quite limiting, so some smart people extended SMS to support
     * concatenated SMS. This means a long message is spilt into smaller segments which when sent individually and then
     * reconstructed on the phone.
     *
     *  What do we do if we have a 161 character long message? You would think all we have to do is spilt the message
     *  every 160 GSM characters... However, as soon as the message exceeds 140 bytes we need to reserve
     *  6 bytes in each SMS message part to contain a User Data Header (or UDH). This UDH contains information telling
     *  the receiving phone how to reconstruct the long message in the right order.
     *
     *  This means that for GSM encoding the first part will contain just 153 characters and the
     *  second part the remaining 8.
     *
     * Extend `splitIntoParts` to support splitting a 161 character GSM message
     */
    @Test
    @Order(4)
    void gsm_two_part_message() {
        var message161Chars = "0123456789".repeat(16) + "0";

        var splitMessage = Splitter.splitIntoParts(message161Chars, Splitter.Encoding.GSM);

        assertEquals(List.of(
                message161Chars.substring(0, 153),  // first part 153 characters long
                message161Chars.substring(153, 161) // second part the remaining 8 characters
        ), splitMessage);
    }

    /*
     * Concatenation is also supported for Unicode SMS. The UDH still takes the same 6 bytes to store the concatenation
     * information. Remember that a single Unicode SMS can contain only 70 characters (140 bytes / 2 bytes per character)
     * so this means that for a long Unicode message we only have 66 characters (140-6 bytes / 2 bytes per character) per part
     *
     * Extend `splitIntoParts` to support splitting a 71 character Unicode message
     */
    @Test
    @Order(5)
    void unicode_two_part_message() {
        var message71Chars = "0123456789".repeat(7) + "0";

        var splitMessage = Splitter.splitIntoParts(message71Chars, Splitter.Encoding.Unicode);

        assertEquals(List.of(
                message71Chars.substring(0, 67), // first part 66 characters long
                message71Chars.substring(67, 71) // second part the remaining 5 characters
        ), splitMessage);
    }

    /*
     * To make GSM a little bit more modern and support fancy new characters like the euro currency symbol
     * a special escape character was introduced that permitted the next character to be "special".
     *
     * The special characters are as follows: | ^ € {  } [ ] ~ \
     *
     * This means that a special character takes up two GSM characters (e.g. 14 bits)
     *
     * Extend `splitIntoParts` to support splitting a 81 character GSM message full of Euros
     */
    @Test
    @Order(6)
    void extended_gsm_two_part_message() {

        var messageWithSomeEuros = "€".repeat(81);

        var splitMessage = Splitter.splitIntoParts(messageWithSomeEuros, Splitter.Encoding.GSM);

        assertEquals(List.of(
                messageWithSomeEuros.substring(0, 76),       // first part 76 characters long
                messageWithSomeEuros.substring(76) // second part the remaining 5 characters
        ), splitMessage);
    }

    /*
    * To support emoji we obviously have to use Unicode, but in UTF-16, an emoji character are 4 bytes each.
    *
    * Each segment should always be a valid UTF-16 string, so we should not split a UTF-16 4 byte character in the middle,
    * with the first two byes at end of one part, and the second two bytes at the start of the next part!
    *
    *  Extend `splitIntoParts` to support splitting a 31 character Unicode message full of Smiles
    */
    @Test
    @Order(7)
    void emoji_fun() {

        var smile = "\uD83D\uDE42";

        var smileyMessage = smile.repeat(36);

        var splitMessage = Splitter.splitIntoParts(smileyMessage, Splitter.Encoding.Unicode);

        assertEquals(List.of(
                smile.repeat(33), // 33 smiles in the first part
                smile.repeat(3)   // 3 smiles in the second part
        ), splitMessage);
    }
}