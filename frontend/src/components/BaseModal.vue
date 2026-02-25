<script setup>
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: "Dialog"
  },
  width: {
    type: String,
    default: "min(520px, 94vw)"
  },
  dismissible: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(["update:modelValue", "close"]);

function closeDialog() {
  emit("update:modelValue", false);
  emit("close");
}

function onOverlayClick(event) {
  if (!props.dismissible) {
    return;
  }

  if (event.target === event.currentTarget) {
    closeDialog();
  }
}
</script>

<template>
  <Teleport to="body">
    <div v-if="modelValue" class="modal-overlay" @click="onOverlayClick">
      <article class="modal-card panel" :style="{ width }" role="dialog" aria-modal="true" :aria-label="title">
        <header class="modal-head">
          <h2 class="modal-title">{{ title }}</h2>
          <button
            v-if="dismissible"
            class="btn btn-secondary modal-close"
            type="button"
            aria-label="Close dialog"
            @click="closeDialog"
          >
            Close
          </button>
        </header>

        <div class="modal-body">
          <slot :close="closeDialog" />
        </div>

        <div class="actions modal-actions">
          <slot name="actions" :close="closeDialog" />
        </div>
      </article>
    </div>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(6, 12, 23, 0.66);
  backdrop-filter: blur(4px);
  display: grid;
  place-items: center;
  padding: 16px;
  z-index: 1200;
}

.modal-card {
  max-height: calc(100vh - 32px);
  overflow: auto;
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.modal-title {
  margin: 0;
}

.modal-close {
  padding: 8px 10px;
  min-width: 0;
}

.modal-body {
  margin-top: 10px;
}

.modal-actions {
  margin-top: 14px;
}
</style>
