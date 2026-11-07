package pe.gob.municipalidad.sigm.common.notification.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendResult {
    private boolean success;
    private String messageId;
    private String errorMessage;
    private String providerName;

    public static SendResult ok(String messageId, String providerName) {
        return SendResult.builder()
                .success(true)
                .messageId(messageId)
                .providerName(providerName)
                .build();
    }

    public static SendResult failure(String errorMessage, String providerName) {
        return SendResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .providerName(providerName)
                .build();
    }

    public static SendResult error(String errorMessage, String providerName) {
        return failure(errorMessage, providerName);
    }
}
