/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bingo;

/**
 *<p></p>
 * @author radoslav
 * @param <T> type of key variable
 */
public class Pair<T> {
    T key;
    String value;
    
    public Pair(T key, String value){
        this.key = key;
        this.value = value;
    }

    public T getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
    
}
