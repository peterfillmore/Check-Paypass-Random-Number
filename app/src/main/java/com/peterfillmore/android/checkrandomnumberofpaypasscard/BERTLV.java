package com.peterfillmore.android.checkrandomnumberofpaypasscard;

import java.util.Arrays;

/**
 * Created by peterfillmore2 on 25/08/2014.
 */
public class BERTLV {
    static public String TAG = "BERTLV";
    private byte[] tag;
    public int taglen; //tag length
    private int length;
    public int totallength;
    private byte[] value;

    public BERTLV(byte[] rawvalue){
        this.tag = new byte[2];
        this.value = new byte[255];
        this.totallength = 0;
        this.length = 0;
        this.taglen = 0;
        decode(rawvalue);
    }

    public void decode(byte[] rawvalue){
        int lenlen = 0;
        int i=0;
        int z=0;
        int length = 0;
        //this.tag[0] = (byte)rawvalue[0];
        if(((byte)rawvalue[0] & (byte)0x1F) == (byte)0x1F){ //tag > 1 byte
            i++;
        }
        this.taglen = i+1;
        System.arraycopy(rawvalue,0,this.tag,0,this.taglen);
        i++;
        if(((byte)rawvalue[i] & 0x80) == 0x80) {
            lenlen = (byte)rawvalue[i] ^ 0x80;
            i++;
            length = (byte)rawvalue[i] & 0xff;
            z = 1;
            while (z < lenlen) {
                i++;
                z++;
                length = length << 8;
                length += (byte)rawvalue[i] & 0xff;
            }
            i++;
        }else {
            length = rawvalue[i] & 0xff;
            i++;
        }
        this.length = length;
        this.totallength = length + i; //total length of the TLV
        System.arraycopy(rawvalue, i, this.value, 0, length);
    }

    public byte[] getTagBytes(){
        return this.tag;
    }
    public int getLength(){
        return this.length;
    }
    public byte[] getValueBytes(){
        return this.value;
    }

    public static String ByteArrayToHexString(byte[] bytes){
        final char[] hexArray = {'0', '1', '2', '3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length *2];
        int v;
        for(int j=0; j<bytes.length; j++){
            v = bytes[j] & 0xFF;
            hexChars[j*2]=hexArray[v>>>4];
            hexChars[j*2+1] = hexArray[v&0x0f];
        }
        return new String(hexChars);
    }

    public static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    public static byte[] findTLVtag(byte[] rawstream, byte[] tag){ //finds the TLV tag specified, returns the value
        byte[] tagvalue = new byte[rawstream.length];
        int lencounter = 0; //counter to track where we are in the template
        //BERTLV initialTemplate = new BERTLV(rawstream); //decode the initial template
        System.arraycopy(rawstream, 0, tagvalue, 0, rawstream.length); //copy input array to the tagvalue array
        while(lencounter < rawstream.length){ //loop
            BERTLV temptag = new BERTLV(tagvalue); //get the first tag.
            byte[] currenttagval = new byte[2];
            System.arraycopy(temptag.getTagBytes(),0,currenttagval,0,temptag.taglen);
            if(Arrays.equals(tag, temptag.getTagBytes())){
                return temptag.getValueBytes();
            }
            lencounter += temptag.totallength;
            System.arraycopy(rawstream, lencounter, tagvalue, 0, (rawstream.length)-lencounter);
        }
        return null;
    }

}
