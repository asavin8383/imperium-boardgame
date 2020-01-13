<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-layout align-center justify-start column fill-height style="border: solid 0 blue">
        <v-container class="px-2 py-1">
            <v-layout class="mb-3" justify-start row>
                <v-card class="pa-3" style="width: 40%; min-width: 350px">
                    <v-flex class="mb-2 pr-2">
                        <v-divider></v-divider>
                        <v-layout class="lineText" row>
                            <v-flex align-self-center md6>
                                Название
                            </v-flex>
                            <v-flex align-self-center md6>
                                Поисковые фразы
                            </v-flex>
                        </v-layout>
                        <v-divider></v-divider>
                    </v-flex>
                </v-card>
            </v-layout>
            <v-layout >
                <v-card class="pa-3" style="width: 100%">
                    <v-card-title>
                        <v-flex md6>
<!--                            <v-text-field-->
<!--                                    v-model="search"-->
<!--                                    append-icon="search"-->
<!--                                    label="Найти"-->
<!--                                    single-line-->
<!--                                    hide-details-->
<!--                                    style="width: 50%"-->
<!--                            ></v-text-field>-->
                        </v-flex>
                        <v-spacer></v-spacer>
                        <v-btn class="icon-btn" color="primary" @click="refresh">
                            <v-icon>refresh</v-icon>
                        </v-btn>
                        <v-btn color="primary" @click="createRecord">СОЗДАТЬ</v-btn>
                    </v-card-title>

                    <dictionary-table ref="dicTable" :hideCheckBox="true" :showActionButtons="true" @actionWithItem="actionWithItem"></dictionary-table>
                </v-card>
            </v-layout>
        </v-container>

        <DialogSearchPhrases v-model="showRecordDialog" :record="chosenRecord" :action=action @success="refresh"></DialogSearchPhrases>
    </v-layout>
</template>

<script>
    import * as VueNotifications from "vue-notifications";
    import DictionaryTable from '../components/DictionaryTable'
    import CheckStatus from '../utils/utils'
    import DialogSearchPhrases from "../components/DialogSearchPhrases";

    export default {
        name: "DirectorySearchPage",
        components: {DictionaryTable, DialogSearchPhrases},

        data() {
            return {
                showRecordDialog: false,
                action: null,
                chosenRecord: {},
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
            showInfoMsg: {
                type: VueNotifications.types.info,
                title: '',
                message: ''
            }
        },

        watch: {
            search() {
                this.pagination.page = 1;
                this.getData();
            },
        },

        computed: {

        },

        methods: {
            actionWithItem(action) {
                if(action.type === "delete")
                    this.deleteRecord(action.item);
                if(action.type === "edit")
                    this.editRecord(action.item);
            },

            deleteRecord({id}) {
                this.$axios.delete(this.$urls.PHRASES + "/" + id).then(()=>{
                    this.$refs.dicTable.refresh();
                }).catch(error=>{
                    console.log('error custom: ', error);
                    if (error.data){
                        if(CheckStatus.checkStatuses400(error))
                            this.showWarnMsg({message: error});
                        else
                            this.showErrorMsg({message: error});
                    }
                });
            },

            createRecord() {
                this.action = "create";
                this.chosenRecord = {};
                this.showRecordDialog = true;
            },

            editRecord(record) {
                this.action = "edit";
                this.chosenRecord = record;
                this.showRecordDialog = true;
            },

            refresh() {
                this.$refs.dicTable.refresh();
            }
        }
    }
</script>

<style scoped>
    .text-style {
        text-align: center;
    }

    .lineText {
        width: 100%;
        height: 45px;
    }

</style>
<style>
    .icon-btn {
        padding: 3px !important;
        height: auto !important;
        cursor: pointer;
    }
    .icon-btn.v-btn {
        min-width: 0 !important;
    }
    .icon-btn .v-icon {
        font-size: 30px;
    }
    .wrap {
        white-space: normal !important;
    }
</style>