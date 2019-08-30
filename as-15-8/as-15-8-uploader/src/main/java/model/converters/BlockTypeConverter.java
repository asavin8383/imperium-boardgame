package model.converters;

import model.enums.BlockType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;


@Converter(autoApply = true)
public class BlockTypeConverter implements AttributeConverter<BlockType, String> {
    @Override
    public String convertToDatabaseColumn(BlockType blockType) {
        return blockType == null ? BlockType.parse(null).getValue() : blockType.getValue();
    }

    @Override
    public BlockType convertToEntityAttribute(String blockType) {
        return BlockType.parse(blockType);
    }
}
