package ru.yandex.cocaine.dealer;

public class TextMessage implements Message{
	private final String message;

	public TextMessage(String message){
		this.message = message;
	}
	
	@Override
	public String toString() {
		return message;
	}
	
}
