package DCT.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Aggro implements Serializable {
    private AggroItem target = null;
    private List<AggroItem> potential = new ArrayList<AggroItem>();

    public boolean hasValidTarget() {
        return target != null && target.getCharacter().isAlive();
    }

    public Character getTargetCharacter() {
        if (hasValidTarget()) {
            return target.getCharacter();
        }
        return null;
    }

    /**
     * Takes a character reference and aggro change value (+/-) and updates an
     * internal aggro list.
     * 
     * @param c
     * @param aggroChange
     * @return True if a new target was set, false otherwise
     */
    public synchronized boolean updateAggro(Character c, long aggroChange) {
        long clientId = c.getClientId();

        final long id = clientId;
        AggroItem check = potential.stream()
                .filter(a -> a.getClientId() == id)
                .findFirst()
                .orElse(null);
        if (check == null) {
            check = new AggroItem();
            check.setCharacter(c);
            check.setClientId(clientId);
            check.setValue(aggroChange);
            potential.add(check);
            if (target == null) {
                target = check;
                return true;
            }
        } else {
            check.changeValue(aggroChange);
        }
        potential = potential.stream()
                // Filter out any null elements from the stream
                .filter(Objects::nonNull)
                // Sort the stream in descending order based on the value returned by 'getValue'
                // method
                // Replace 'MyClass' with the actual class name and 'getValue' with the actual
                // method name
                .sorted(Comparator.comparingLong(AggroItem::getValue).reversed())
                // Collect the sorted stream back into a list
                // The 'Collectors.toList()' method ensures that the list is mutable
                .collect(Collectors.toList());
        if (potential.size() > 0) {
            AggroItem first = potential.get(0);
            if (first != null && first != target && first.getCharacter().isAlive()) {
                target = first;
                return true;
            }
        }
        return false;
    }

    private class AggroItem implements Serializable {
        private Character character;
        private long clientId;
        private long value;

        public void setCharacter(Character c) {
            this.character = c;
        }

        public Character getCharacter() {
            return this.character;
        }

        public void setClientId(long id) {
            clientId = id;
        }

        public long getClientId() {
            return clientId;
        }

        public void setValue(long v) {
            value = v;
        }

        public void changeValue(long change) {
            value += change;
            if (value <= 0) {
                value = 0;
            }
        }

        public long getValue() {
            return value;
        }
    }
}
