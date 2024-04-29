package lt.biip.auth.dto;

public record UserDataResponse(
        String firstName,
        String lastName,
        String personalCode,
        String email,
        String phoneNumber,
        String companyCode,
        String companyName
) {
}