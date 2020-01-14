<template>
    <v-dialog v-model="value" lazy persistent width="45%">
        <v-card class="record-card pa-4">
            <v-card-title class="font-weight-bold pa-0 pb-4">
                <v-flex align-self-center md6 style="font-size: larger">
                    {{ action === 'create' ? 'Создать поисковую фразу' : 'Редактировать поисковую фразу'}}
                </v-flex>
                <v-spacer></v-spacer>
                <v-icon @click="closeCard">close</v-icon>
            </v-card-title>
            <v-card-text class="pa-0">

                <v-layout align-end row fill-height class="pa-0 mb-2">
                    <v-flex md5 class="name-font">Поисковая фраза</v-flex>
                    <v-flex md7 class="name-font">
                        <v-text-field v-model="phrase" hide-details></v-text-field>
                    </v-flex>
                </v-layout>
            </v-card-text>
            <v-card-actions>
                <v-layout column class="pt-3">
                    <v-layout row>
                        <v-spacer></v-spacer>
                        <v-btn color="" flat @click="closeCard">Отмена</v-btn>
                        <v-btn color="primary" @click="save" :disabled="!validForm">Сохранить</v-btn>
                    </v-layout>
                </v-layout>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
    import * as VueNotifications from "vue-notifications";
    import CheckStatus from '../utils/utils'

    export default {
        name: "DialogSearchPhrases",
        props: ['value', "action", "record"],

        data() {
            return {
                phrase: ""
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

        watch: {
            value() {
                if(this.action === "edit")
                    this.phrase = this.record.phrase;
                else
                    this.phrase = "";
            },
        },

        computed: {
            validForm() {
                return this.phrase.length > 0;
            }
        },

        methods: {

            closeCard() {
                this.$emit('input', false);
                this.phrase = "";
            },

            emitSuccess() {
                this.$emit('success');
            },

            save() {
                if(this.action === "edit"){
                    let data = {
                        "phrase": this.phrase,
                        "subtypeId": "1"
                    };

                    this.$axios.put(this.$urls.PHRASES + "/" + this.record.id, data).then(resp => {
                        this.closeCard();
                        this.emitSuccess();
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
                }else{
                    let data = {
                        "phrase": this.phrase,
                        "subtypeId": 1
                    };

                    this.$axios.post(this.$urls.PHRASES, data).then(resp => {
                        this.closeCard();
                        this.emitSuccess();
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
    }
</script>

<style>
    .value-font .v-input .v-textarea {
        display: none;
    }
    .value-font .v-input.v-textarea {
        padding-top: 0;
    }

    .value-font .v-input.v-input--checkbox {
        margin: 0;
        padding: 0;
    }
    .value-font .v-input.v-input--checkbox .v-messages{
        display: none;
    }

    .value-font .v-text-field__details {
        display: none;
    }
</style>

<style scoped>
    .name-font {
        color: rgba(0,0,0,.54);
    }
</style>