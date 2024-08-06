package lt.biip.auth.dto;

public record GenerateTicketResponse(
        String ticket,
        String host,
        String url
) {
}