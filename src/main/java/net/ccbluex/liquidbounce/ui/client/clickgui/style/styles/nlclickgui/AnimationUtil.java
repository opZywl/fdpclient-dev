package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui;

public class AnimationUtil {
   public static float calculateCompensation(float target, float current, long delta, int speed) {
      float diff = current - target;
      if (delta < 1L) {
         delta = 1L;
      }

      double xD;
      if (diff > (float)speed) {
         xD = (double)((long)speed * delta / 16L) < 0.25D ? 0.5D : (double)((long)speed * delta / 16L);
         current = (float)((double)current - xD);
         if (current < target) {
            current = target;
         }
      } else if (diff < (float)(-speed)) {
         xD = (double)((long)speed * delta / 16L) < 0.25D ? 0.5D : (double)((long)speed * delta / 16L);
         current = (float)((double)current + xD);
         if (current > target) {
            current = target;
         }
      } else {
         current = target;
      }

      return current;
   }
   public static double animate(double target, double current, double speed) {
      boolean larger;
      boolean bl = larger = target > current;
      if (speed < 0.0) {
         speed = 0.0;
      } else if (speed > 1.0) {
         speed = 1.0;
      }
      double dif = Math.max((double)target, (double)current) - Math.min((double)target, (double)current);
      double factor = dif * speed;
      if (factor < 0.1) {
         factor = 0.1;
      }
      current = larger ? (current += factor) : (current -= factor);
      return current;
   }
}
