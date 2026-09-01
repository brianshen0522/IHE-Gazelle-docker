package net.ihe.gazelle.validation.gateway.business;

import java.util.Objects;

public class ProfileReadId {

   private final String profileId;
   private final String serviceName;

   public ProfileReadId(String profileId, String serviceName) {
      this.profileId = profileId;
      this.serviceName = serviceName;
   }

   public String getProfileId() {
      return profileId;
   }

   public String getServiceName() {
      return serviceName;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      ProfileReadId that = (ProfileReadId) o;
      return Objects.equals(profileId, that.profileId) && Objects.equals(serviceName, that.serviceName);
   }

   @Override
   public int hashCode() {
      return Objects.hash(profileId, serviceName);
   }
}
