<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-dialog v-model="isActive" lazy persistent width="45%">
        <v-card class="record-card pa-3">
            <v-card-title class="font-weight-bold pa-0 pb-4">
                <v-flex align-self-center md6 style="font-size: larger">
                    Удалить запись
                </v-flex>
                <v-spacer></v-spacer>
                <v-icon @click="closeCard">close</v-icon>
            </v-card-title>
            <v-card-text class="pa-0">
                <v-layout row class="pa-0 mb-2">
                    Вы уверены, что хотите удалить запись&nbsp;<b>№ {{ record && record[this.fieldId]}} "{{ record && record[this.fieldName] }}" </b>?
                </v-layout>
            </v-card-text>
            <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn color="" flat @click="closeCard">Отмена</v-btn>
                <v-btn color="error" @click="deleteRecord">Да, удалить</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>
<script>
    import Toggleable from "vuetify/lib/mixins/toggleable";
    import * as VueNotifications from "vue-notifications";
    import CheckStatus from '../utils/utils'

    export default {
        name: "DialogDeleteRecord",

        mixins: [Toggleable],

        props: ['value', "record", 'type', 'del-url', 'field-id', 'field-name'],

        data() {
            return {
            }
        },

        notifications: {
            showSuccessMsg: {
                type: VueNotifications.types.success,
                title: '',
                message: '',
            },
            showErrorMsg: {
                type: VueNotifications.types.error,
                title: '',
                message: '',
            },
            showWarnMsg: {
                type: VueNotifications.types.warn,
                title: '',
                message: '',
            },
        },

        methods: {
            closeCard() {
                this.isActive = false;
            },

            deleteRecord() {
                const params = {origId: this.record.origId};
                const url = this.delUrl ? this.delUrl
                          : this.type === 'ps' ? this.$urls.PS_INFO
                          : this.type === 'pasd' ? this.$urls.PASD_INFO
                          : this.type === 'user_erdi' ? '/erdi/custom/' + this.record.id
                          : "nowhere";
                this.$axios.delete(url, {params}).then(resp => {
                    this.showSuccessMsg({ message: "Запись успешно удалена"});
                    this.closeCard();
                    this.$emit('refreshData');
                }).catch(e => {
                    console.log('error ', e);
                    if(CheckStatus.checkStatus403(e, this.showWarnMsg))return;
                    if (e.data){
                        if(CheckStatus.checkStatuses400(e))
                            this.showWarnMsg({message: e.data.message});
                        else
                            this.showErrorMsg({message: e.data.message});
                    }
                });
            }
        }
    }
</script>

<style>

</style>